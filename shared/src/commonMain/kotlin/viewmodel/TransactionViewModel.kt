package viewmodel

import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import com.banking.shared.data.TransactionUiState
import data.TransactionRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for managing transaction state and business logic.
 * Shared across Android and iOS platforms.
 */
class TransactionViewModel(
    private val repository: TransactionRepo
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    /**
     * Load all transactions from the repository.
     */
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            repository.getAllTransactions().collectLatest { transactions ->
                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    loading = false
                )
            }
        }
    }

    /**
     * Add new transactions, auto-categorizing them.
     */
    fun addTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            val categorized = transactions.map { tx ->
                val category = CategoryMapper.shared.categorizeTransaction(tx.description)
                tx.copy(category = category)
            }
            repository.insertTransactions(categorized)
        }
    }

    /**
     * Update the category of a specific transaction.
     */
    fun updateCategory(transactionId: String, category: String) {
        viewModelScope.launch {
            repository.updateCategory(transactionId, category)
        }
    }

    /**
     * Set the filter for transactions.
     */
    fun setFilter(filter: TransactionFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        applyFilter(filter)
    }

    /**
     * Set the PDF file name.
     */
    fun setPdfFileName(fileName: String?) {
        _uiState.value = _uiState.value.copy(pdfFileName = fileName)
    }

    /**
     * Clear all transactions.
     */
    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }

    /**
     * Apply filter to transactions.
     */
    private fun applyFilter(filter: TransactionFilter) {
        viewModelScope.launch {
            var flow = repository.getAllTransactions()

            if (filter.category != "all") {
                flow = repository.getTransactionsByCategory(filter.category)
            }

            filter.month?.let { month ->
                flow = repository.getTransactionsByMonth(month)
            }

            if (filter.search.isNotBlank()) {
                flow = repository.searchTransactions(filter.search)
            }

            flow.collectLatest { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }

    /**
     * Get summary statistics for transactions.
     */
    fun getSummaryStats(): TransactionSummary {
        val transactions = _uiState.value.transactions
        val total = transactions.sumOf { it.amount }
        val byCategory = transactions.groupBy(
            { it.category },
            { it.amount }
        ).mapValues { it.value.sum() }

        return TransactionSummary(
            totalAmount = total,
            transactionCount = transactions.size,
            amountByCategory = byCategory
        )
    }
}

/**
 * Summary statistics for transactions.
 */
data class TransactionSummary(
    val totalAmount: Double,
    val transactionCount: Int,
    val amountByCategory: Map<String, Double>
)