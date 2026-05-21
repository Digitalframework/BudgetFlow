package data

import com.banking.shared.data.Transaction
import kotlinx.coroutines.flow.Flow



/**
 * Repository interface for transaction data operations.
 * Platform-specific implementations provided in androidMain and iosMain.
 */
interface TransactionRepo {

    /**
     * Get all transactions as a Flow.
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions by ID.
     */
    suspend fun getTransactionById(id: String): Transaction?

    /**
     * Insert a single transaction.
     */
    suspend fun insertTransaction(transaction: Transaction)

    /**
     * Insert multiple transactions.
     */
    suspend fun insertTransactions(transactions: List<Transaction>)

    /**
     * Update a transaction's category.
     */
    suspend fun updateCategory(id: String, category: String)

    /**
     * Delete a transaction by ID.
     */
    suspend fun deleteTransaction(id: String)

    /**
     * Delete all transactions.
     */
    suspend fun deleteAllTransactions()

    /**
     * Get transactions filtered by category.
     */
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    /**
     * Get transactions filtered by month.
     */
    fun getTransactionsByMonth(yearMonth: String): Flow<List<Transaction>>

    /**
     * Search transactions by description.
     */
    fun searchTransactions(query: String): Flow<List<Transaction>>
}

/**
 * Result wrapper for repository operations.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Internal MongoDB document representation (common expect/actual).
 */
expect class TransactionDocument {
    val _id: String
    val id: String
    val date: String
    val description: String
    val amount: Double
    val category: String
    val rawLine: String
    val createdAt: Long
    val updatedAt: Long
    
    fun toTransaction(): Transaction
    
    companion object {
        fun fromTransaction(transaction: Transaction): TransactionDocument
    }
    
    constructor(
        _id: String,
        id: String,
        date: String,
        description: String,
        amount: Double,
        category: String,
        rawLine: String,
        createdAt: Long,
        updatedAt: Long
    )
}

// Factory function is platform-specific due to different dependencies:
// - Android: fun createTransactionRepository(context: Context): TransactionRepo
// - iOS: fun createTransactionRepository(): TransactionRepo
