package store

import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import kotlinx.browser.localStorage
import kotlinx.browser.window

private const val STORAGE_KEY_TRANSACTIONS = "banking_transactions"
private const val STORAGE_KEY_FILTER = "banking_filter"
private const val STORAGE_KEY_PDF_NAME = "banking_pdf_filename"

class WebStore {

    private var transactions: MutableList<Transaction> = mutableListOf()
    private var loading: Boolean = false
    private var filter: TransactionFilter = TransactionFilter()
    private var pdfFileName: String? = null

    private val listeners: MutableList<() -> Unit> = mutableListOf()

    fun subscribe(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
        saveState()
    }

    fun getTransactions(): List<Transaction> = transactions.toList()
    fun isLoading(): Boolean = loading
    fun getFilter(): TransactionFilter = filter
    fun getPdfFileName(): String? = pdfFileName

    fun setLoading(isLoading: Boolean) {
        loading = isLoading
        notifyListeners()
    }

    fun setFilter(newFilter: TransactionFilter) {
        filter = newFilter
        notifyListeners()
    }

    fun setPdfFileName(name: String?) {
        pdfFileName = name
        notifyListeners()
    }

    fun addTransactions(newTransactions: List<Transaction>) {
        val existingIds = transactions.map { it.id }.toSet()
        val uniqueTransactions = newTransactions.filter { it.id !in existingIds }
        transactions.addAll(uniqueTransactions)
        notifyListeners()
    }

    fun updateCategory(transactionId: String, category: String) {
        val index = transactions.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            val updated = transactions[index].copy(category = category)
            transactions[index] = updated
            notifyListeners()
        }
    }

    fun clearAll() {
        transactions.clear()
        pdfFileName = null
        notifyListeners()
    }

    fun loadPersistedData() {
        try {
            val savedTransactions = localStorage.getItem(STORAGE_KEY_TRANSACTIONS)
            if (savedTransactions != null) {
                transactions = parseTransactions(savedTransactions).toMutableList()
            }

            val savedFilter = localStorage.getItem(STORAGE_KEY_FILTER)
            if (savedFilter != null) {
                filter = parseFilter(savedFilter)
            }

            pdfFileName = localStorage.getItem(STORAGE_KEY_PDF_NAME)
        } catch (e: Exception) {
            //window.console.error("Error loading persisted data: ${e.message}")
        }
        notifyListeners()
    }

    private fun saveState() {
        try {
            localStorage.setItem(STORAGE_KEY_TRANSACTIONS, serializeTransactions(transactions))
            localStorage.setItem(STORAGE_KEY_FILTER, serializeFilter(filter))
            if (pdfFileName != null) {
                localStorage.setItem(STORAGE_KEY_PDF_NAME, pdfFileName!!)
            } else {
                localStorage.removeItem(STORAGE_KEY_PDF_NAME)
            }
        } catch (e: Exception) {
            //window.console.error("Error saving state: ${e.message}")
        }
    }

    // FIX 1: Build the list of JSON strings first, then pass to buildJsonArray
    private fun serializeTransactions(transactions: List<Transaction>): String {
        val items = transactions.map { tx ->
            """{"id":"${tx.id}","date":"${tx.date}","description":"${tx.description.escapeJson()}","amount":${tx.amount},"category":"${tx.category}","rawLine":"${tx.rawLine.escapeJson()}"}"""
        }
        return buildJsonArray(items)
    }

    private fun parseTransactions(json: String): List<Transaction> {
        val result = mutableListOf<Transaction>()
        val regex = Regex("""\{[^}]+\}""")
        regex.findAll(json).forEach { match ->
            val obj = match.value
            val id = Regex(""""id":"([^"]+)"""").find(obj)?.groupValues?.get(1) ?: ""
            val date = Regex(""""date":"([^"]+)"""").find(obj)?.groupValues?.get(1) ?: ""
            val description = Regex(""""description":"([^"]+)"""").find(obj)?.groupValues?.get(1) ?: ""
            val amount = Regex(""""amount":(-?[0-9.]+)""").find(obj)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val category = Regex(""""category":"([^"]+)"""").find(obj)?.groupValues?.get(1) ?: "Sonstiges"
            val rawLine = Regex(""""rawLine":"([^"]*)"""").find(obj)?.groupValues?.get(1) ?: ""

            if (id.isNotEmpty()) {
                result.add(Transaction(id, date, description, amount, category, rawLine))
            }
        }
        return result
    }

    private fun serializeFilter(filter: TransactionFilter): String {
        return """{"category":"${filter.category}","month":${if (filter.month != null) "\"${filter.month}\"" else "null"},"search":"${filter.search.escapeJson()}"}"""
    }

    private fun parseFilter(json: String): TransactionFilter {
        val category = Regex(""""category":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "all"
        val month = Regex(""""month":"([^"]+)"""").find(json)?.groupValues?.get(1)
        val search = Regex(""""search":"([^"]*)"""").find(json)?.groupValues?.get(1) ?: ""
        return TransactionFilter(category, month, search)
    }

    private fun String.escapeJson(): String {
        return this.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun buildJsonArray(items: List<String>): String {
        return "[${items.joinToString(",")}]"
    }
}

val webStore = WebStore()