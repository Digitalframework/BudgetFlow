package utils

import com.banking.shared.data.AccountBalance
import com.banking.shared.data.BalanceKind
import com.banking.shared.data.Transaction

object BankStatementParser {

    private val TX_HEADER = Regex("""^(\d{2}\.\d{2}\.(?:\d{2,4})?)\s+(\d{2}\.\d{2}\.(?:\d{2,4})?)\s+(.*)""")
    private val AMOUNT_AT_END = Regex("""(\d{1,3}(?:\.\d{3})*,\d{2})\s*([SsHh+-]?)\s*$""")
    private val SKIP_LINE = Regex(
        """(\d{2}:\d{2}:\d{2}|REF\s+\d|ECTL|GIR\s+\d|PN:\d|\d{6,}|Kartenzahlung|girocard|Lastschrift|Überweisung|SEPA|Dauerauftrag|Gutschrift|Einzug|[ÜU]bertrag|Kontostand|Blatt\s+\d|Kontoauszug|Kontokorrent|Kontonummer|Bankleitzahl|Bu-Tag|Bitte beachten)""",
        RegexOption.IGNORE_CASE
    )

    // A sheet break splits a booking from its detail lines: the sheet ends with
    // "Übertrag auf Blatt N", the next one opens with the bank address, the
    // column headers and "Übertrag von Blatt N-1" before the booking continues.
    private val PAGE_BREAK_START = Regex("""[ÜU]bertrag\s+auf\s+Blatt""", RegexOption.IGNORE_CASE)
    private val PAGE_BREAK_END = Regex("""[ÜU]bertrag\s+von\s+Blatt""", RegexOption.IGNORE_CASE)

    /** Generous bound on the furniture between two sheets; the real block is ~15 lines. */
    private const val MAX_PAGE_BREAK_LINES = 40

    /**
     * Drop the carry-forward block between two sheets.
     *
     * Without it the "Übertrag auf Blatt 3   1.047,71 H" footer is the first
     * continuation line of the booking at the bottom of the sheet, so it becomes
     * that booking's merchant name — and the carry-forward total, which is a
     * running balance and not a payment, reads as if it were the merchant.
     */
    fun stripPageBreaks(lines: List<String>): List<String> {
        val kept = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            if (PAGE_BREAK_START.containsMatchIn(lines[i])) {
                val end = (i + 1 until minOf(lines.size, i + 1 + MAX_PAGE_BREAK_LINES))
                    .firstOrNull { PAGE_BREAK_END.containsMatchIn(lines[it]) }
                // No matching header means this was the last sheet's footer —
                // drop that one line rather than the rest of the document.
                i = (end ?: i) + 1
                continue
            }

            kept.add(lines[i])
            i++
        }

        return kept
    }

    /**
     * "alter Kontostand vom 30.12.2025" / "neuer Kontostand vom 31.01.2026".
     * The day is normally the 30th or 31st, but a short month closes on the 28th
     * or 29th, so the day itself is not part of the match.
     */
    private val BALANCE_HEADER = Regex(
        """(alter|neuer)\s+Kontostand\s+vom\s+(\d{1,2}\.\d{1,2}\.\d{2,4})(.*)""",
        RegexOption.IGNORE_CASE
    )

    /** A balance always carries its H/S indicator; without one it is not a balance. */
    private val BALANCE_AMOUNT = Regex("""(\d{1,3}(?:\.\d{3})*,\d{2})\s*([SsHh])(?![A-Za-z])""")

    /**
     * Opening and closing balances of the statement.
     *
     * The amount usually sits on the same extracted line as the label, but a
     * PDF that puts the figure on its own text row would break that, so the
     * next two lines are searched as a fallback.
     */
    fun parseBalances(lines: List<String>): List<AccountBalance> {
        val balances = mutableListOf<AccountBalance>()

        lines.forEachIndexed { index, line ->
            val header = BALANCE_HEADER.find(line) ?: return@forEachIndexed
            val date = parseDateStr(header.groupValues[2]) ?: return@forEachIndexed

            val amount = balanceAmount(header.groupValues[3])
                ?: lines.drop(index + 1).take(2).firstNotNullOfOrNull { balanceAmount(it) }
                ?: return@forEachIndexed

            val kind = if (header.groupValues[1].lowercase().startsWith("alter")) {
                BalanceKind.OPENING
            } else {
                BalanceKind.CLOSING
            }

            balances.add(AccountBalance(date = date, amount = amount, kind = kind))
        }

        return balances.distinctBy { "${it.kind}_${it.date}" }
    }

    /** Signed balance: "H" (Haben) is credit, "S" (Soll) an overdraft. */
    private fun balanceAmount(text: String): Double? {
        val match = BALANCE_AMOUNT.find(text) ?: return null
        val value = match.groupValues[1].replace(".", "").replace(",", ".").toDoubleOrNull()
            ?: return null
        return if (match.groupValues[2].uppercase() == "S") -value else value
    }

    fun parseDateStr(raw: String): String? {
        val match = Regex("""(\d{2})\.(\d{2})\.(\d{2,4})?""").find(raw) ?: return null
        val dd = match.groupValues[1]
        val mm = match.groupValues[2]
        var yyyy = match.groupValues[3]
        yyyy = when {
            yyyy.isEmpty() -> "2026"
            yyyy.length == 2 -> "20$yyyy"
            else -> yyyy
        }
        return "$yyyy-$mm-$dd"
    }

    fun parseAmount(amountStr: String, indicator: String): Double? {
        val num = amountStr.replace(".", "").replace(",", ".").toDoubleOrNull() ?: return null
        val ind = indicator.trim().uppercase()
        if (ind == "H" || ind == "+") return null
        return kotlin.math.abs(num)
    }

    fun extractMerchantName(contLines: List<String>): String? {
        for (line in contLines) {
            val clean = line.trim()
            if (clean.length < 2) continue
            if (SKIP_LINE.containsMatchIn(clean)) continue

            val withoutLocation = clean
                .replace(Regex("""\s*/[A-Z]{2}\s*$"""), "")
                .split("/")[0]
                .trim()

            val name = withoutLocation.take(50)
            if (name.length >= 2 && !name.matches(Regex("""^\d+$"""))) {
                return toTitleCase(name)
            }
        }
        return null
    }

    fun toTitleCase(str: String): String {
        return str
            .lowercase()
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .replace(Regex("""\bGmbh\b"""), "GmbH")
            .replace(Regex("""\bAg\b"""), "AG")
            .replace(Regex("""\bKg\b"""), "KG")
            .trim()
    }

    fun parseTransactions(rawLines: List<String>): List<Transaction> {
        val lines = stripPageBreaks(rawLines)
        val transactions = mutableListOf<Transaction>()
        var id = 0
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val headerMatch = TX_HEADER.find(line)

            if (headerMatch != null) {
                val date1Raw = headerMatch.groupValues[1]
                val rest = headerMatch.groupValues[3]
                val dateStr = parseDateStr(date1Raw)

                val amountMatch = AMOUNT_AT_END.find(rest)
                if (amountMatch == null) {
                    i++
                    continue
                }

                val amount = parseAmount(amountMatch.groupValues[1], amountMatch.groupValues[2])
                if (amount == null) {
                    i++
                    continue
                }

                val contLines = mutableListOf<String>()
                var j = i + 1
                while (j < lines.size && !TX_HEADER.containsMatchIn(lines[j])) {
                    contLines.add(lines[j])
                    j++
                }

                var description = extractMerchantName(contLines)

                if (description == null) {
                    // ✅ Fix: use String.replace(Regex, String) overload
                    val headerText = rest.replace(AMOUNT_AT_END, "").trim()
                    val cleaned = headerText
                        .replace(Regex("""Kartenzahlung\s+girocard\s+PN:\d+""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""SEPA\s+\S+""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""Lastschrift\s+\S+""", RegexOption.IGNORE_CASE), "")
                        .trim()
                    description = if (cleaned.isNotEmpty()) toTitleCase(cleaned.take(50)) else "Unbekannt"
                }

                if (dateStr != null) {
                    transactions.add(
                        Transaction(
                            id = "tx_${id++}_${dateStr}_${kotlin.math.abs(amount)}",
                            date = dateStr,
                            description = description,
                            amount = amount,
                            rawLine = line.take(120)
                        )
                    )
                }

                i = j
            } else {
                i++
            }
        }

        if (transactions.isEmpty()) {
            return parseFallback(lines)
        }

        return dedup(transactions)
    }

    fun parseFallback(lines: List<String>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        var id = 0

        for (line in lines) {
            val dateMatch = Regex("""(\d{2}\.\d{2}\.(?:\d{2,4})?)""").find(line)
            val amountMatch = AMOUNT_AT_END.find(line)
            if (dateMatch == null || amountMatch == null) continue

            val dateStr = parseDateStr(dateMatch.groupValues[1])
            val amount = parseAmount(amountMatch.groupValues[1], amountMatch.groupValues[2])

            // ✅ Fix: use String.replace(Regex, String) overload
            var description = line
                .replace(Regex(Regex.escape(dateMatch.groupValues[0])), "")
                .replace(AMOUNT_AT_END, "")
                .replace(Regex("""\s+"""), " ")
                .trim()
            description = if (description.isNotEmpty()) toTitleCase(description.take(60)) else "Unbekannt"

            if (dateStr != null && amount != null) {
                transactions.add(
                    Transaction(
                        id = "tx_${id++}_${dateStr}_${kotlin.math.abs(amount)}",
                        date = dateStr,
                        description = description,
                        amount = amount,
                        rawLine = line.take(80)
                    )
                )
            }
        }

        return dedup(transactions)
    }

    // ✅ Fix: was filtering OUT the first occurrence due to add() return value
    private fun dedup(txs: List<Transaction>): List<Transaction> {
        val seen = mutableSetOf<String>()
        return txs.filter { t ->
            val key = "${t.date}_${t.amount}_${t.description.take(20)}"
            seen.add(key) // returns true only on first insertion → keep; false on duplicate → drop
        }
    }
}