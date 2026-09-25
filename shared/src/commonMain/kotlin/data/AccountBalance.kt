package com.banking.shared.data

/**
 * A balance line of a statement ("alter Kontostand vom 30.12.2025   2.264,27 H").
 *
 * [amount] is signed the way the statement means it: "H" (Haben) is money in
 * the account, "S" (Soll) is an overdraft and therefore negative.
 */
data class AccountBalance(
    val date: String,
    val amount: Double,
    val kind: BalanceKind,
)

enum class BalanceKind {
    /** "alter Kontostand vom …" — the balance the statement period started at. */
    OPENING,

    /** "neuer Kontostand vom …" — the balance the statement period ended at. */
    CLOSING,
}

/**
 * How much was put aside between two balance dates: the later balance minus the
 * earlier one. Positive means saved, negative means spent down.
 */
data class SavingsComparison(
    val from: AccountBalance,
    val to: AccountBalance,
    val difference: Double,
    /** How many statements the comparison had to choose from. */
    val statementCount: Int,
)

object SavingsCalculator {

    /**
     * Every month-end balance of every imported statement, oldest first.
     *
     * A statement's closing balance is the next statement's opening balance, so
     * the two carry the same date and the same figure — one entry per date is
     * enough, and the result reads as a continuous timeline of month-end stands.
     */
    fun timeline(balances: List<AccountBalance>): List<AccountBalance> =
        balances.sortedBy { it.date }.distinctBy { it.date }

    /** Distinct opening balances — exactly one per imported statement. */
    fun openingBalances(balances: List<AccountBalance>): List<AccountBalance> =
        balances
            .filter { it.kind == BalanceKind.OPENING }
            .distinctBy { it.date }
            .sortedBy { it.date }

    /**
     * The saved amount up to [month] ("yyyy-MM"), or up to the newest balance
     * when [month] is null: the oldest known balance subtracted from the last
     * balance that falls in [month] or earlier.
     *
     * Picking May 2026 therefore compares against December 2025 — the stand the
     * very first imported statement started from.
     *
     * Returns null when there is nothing to compare: fewer than two statements
     * (a single one only knows its own period), or a [month] that lies at or
     * before the first balance.
     */
    fun compare(balances: List<AccountBalance>, month: String? = null): SavingsComparison? {
        val statementCount = openingBalances(balances).size
        if (statementCount < 2) return null

        val timeline = timeline(balances)
        val from = timeline.firstOrNull() ?: return null
        val to = if (month == null) {
            timeline.lastOrNull()
        } else {
            timeline.lastOrNull { it.date.take(7) <= month }
        } ?: return null

        if (to.date <= from.date) return null

        return SavingsComparison(
            from = from,
            to = to,
            difference = to.amount - from.amount,
            statementCount = statementCount,
        )
    }

    /** Merge freshly parsed balances into the stored ones, dropping re-imports. */
    fun merge(existing: List<AccountBalance>, incoming: List<AccountBalance>): List<AccountBalance> =
        (existing + incoming)
            .distinctBy { "${it.kind}_${it.date}" }
            .sortedBy { it.date }
}
