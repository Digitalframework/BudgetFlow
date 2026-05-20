package utils

import com.banking.shared.data.Transaction

object BankStatementParser {

    private val TX_HEADER = Regex("""^(\d{2}\.\d{2}\.(?:\d{2,4})?)\s+(\d{2}\.\d{2}\.(?:\d{2,4})?)\s+(.*)""")
    private val AMOUNT_AT_END = Regex("""(\d{1,3}(?:\.\d{3})*,\d{2})\s*([SsHh+-]?)\s*$""")
    private val SKIP_LINE = Regex(
        """(\d{2}:\d{2}:\d{2}|REF\s+\d|ECTL|GIR\s+\d|PN:\d|\d{6,}|Kartenzahlung|girocard|Lastschrift|Überweisung|SEPA|Dauerauftrag|Gutschrift|Einzug)""",
        RegexOption.IGNORE_CASE
    )

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

    fun parseTransactions(lines: List<String>): List<Transaction> {
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