package com.banking.app.data

import android.content.Context

/**
 * Monthly spending limit per category, persisted on the device — the Android
 * counterpart of the `banking_budgets` entry the web app keeps in localStorage.
 * A missing key means "not set yet".
 */
class BudgetStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("banking_budgets", Context.MODE_PRIVATE)

    fun load(): Map<String, Double> =
        prefs.all.mapNotNull { (category, raw) ->
            val limit = (raw as? String)?.toDoubleOrNull()
            if (limit != null && limit > 0.0) category to limit else null
        }.toMap()

    /** Passing a null or non-positive limit clears the budget for that category. */
    fun set(category: String, limit: Double?): Map<String, Double> {
        prefs.edit().apply {
            if (limit == null || limit <= 0.0) remove(category) else putString(category, limit.toString())
        }.apply()
        return load()
    }
}
