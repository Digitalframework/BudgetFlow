package com.banking.app.data

import android.content.Context
import com.banking.shared.data.AccountBalance
import com.banking.shared.data.BalanceKind

/**
 * Opening/closing balances of every imported statement, persisted on the device
 * — the Android counterpart of the `banking_balances` entry the web app keeps in
 * localStorage.
 *
 * One entry per `kind_date`, so re-importing the same statement changes nothing.
 */
class BalanceStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("banking_balances", Context.MODE_PRIVATE)

    fun load(): List<AccountBalance> =
        prefs.all.mapNotNull { (key, raw) ->
            val amount = (raw as? String)?.toDoubleOrNull() ?: return@mapNotNull null
            val kindName = key.substringBefore('_', missingDelimiterValue = "")
            val date = key.substringAfter('_', missingDelimiterValue = "")
            val kind = BalanceKind.entries.find { it.name == kindName }
            if (kind != null && date.isNotEmpty()) AccountBalance(date, amount, kind) else null
        }.sortedBy { it.date }

    fun add(balances: List<AccountBalance>): List<AccountBalance> {
        if (balances.isEmpty()) return load()
        prefs.edit().apply {
            balances.forEach { putString("${it.kind.name}_${it.date}", it.amount.toString()) }
        }.apply()
        return load()
    }

    fun clear(): List<AccountBalance> {
        prefs.edit().clear().apply()
        return emptyList()
    }
}
