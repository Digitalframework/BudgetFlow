package com.banking.app.ui.format

import java.text.NumberFormat
import java.util.Locale

/**
 * Number and date formatting, mirroring `design/Theme.kt` on the web so both
 * clients print the same strings for the same data.
 */

private val eur: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private val eur0: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 0
}

fun fmtEur(n: Double?): String = eur.format(n ?: 0.0)

fun fmtEurShort(n: Double?): String = eur0.format(n ?: 0.0)

/** 0.184 → "18 %", 0.043 → "4,3 %" */
fun fmtPct(n: Double): String {
    val digits = if (n < 0.1) 1 else 0
    return "${fmtFixed(n * 100, digits)} %"
}

fun fmtFixed(n: Double, digits: Int = 0): String =
    String.format(Locale.GERMANY, "%.${digits}f", n)

private val MONTHS = arrayOf(
    "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
    "Jul", "Aug", "Sep", "Okt", "Nov", "Dez",
)

/** "2026-04" → "Apr 2026" */
fun fmtMonth(ym: String?): String {
    if (ym.isNullOrEmpty()) return ""
    val parts = ym.split("-")
    if (parts.size < 2) return ym
    val m = parts[1].toIntOrNull()
    val name = if (m != null && m in 1..12) MONTHS[m - 1] else parts[1]
    return "$name ${parts[0]}"
}

/** "2026-04-13" → "13. Apr" */
fun fmtDay(iso: String?): String {
    if (iso.isNullOrEmpty()) return ""
    val parts = iso.split("-")
    if (parts.size < 3) return iso
    val m = parts[1].toIntOrNull()
    val name = if (m != null && m in 1..12) MONTHS[m - 1] else parts[1]
    val d = parts[2].toIntOrNull()?.toString() ?: parts[2]
    return "$d. $name"
}
