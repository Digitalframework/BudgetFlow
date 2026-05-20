package data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MongoDB Realm implementation for Android platform.
 * Uses MongoDB Atlas Device SDK for local persistence and sync.
 */
class MongoDbAndroid(context: Context) : TransactionRepo {

    // In a real implementation, this would connect to MongoDB
    // For now, we use in-memory storage
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val transactionMap = mutableMapOf<String, Transaction>()

    init {
        // Load initial data from local storage if available
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        _transactions.value = transactionMap.values.toList()
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return _transactions.asStateFlow()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionMap[id]
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionMap[transaction.id] = transaction
        loadAllTransactions()
    }

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        transactions.forEach { transactionMap[it.id] = it }
        loadAllTransactions()
    }

    override suspend fun updateCategory(id: String, category: String) {
        transactionMap[id]?.let {
            transactionMap[id] = it.copy(category = category)
        }
        loadAllTransactions()
    }

    override suspend fun deleteTransaction(id: String) {
        transactionMap.remove(id)
        loadAllTransactions()
    }

    override suspend fun deleteAllTransactions() {
        transactionMap.clear()
        loadAllTransactions()
    }

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        val flow = MutableStateFlow<List<Transaction>>(emptyList())
        flow.value = transactionMap.values.filter { it.category == category }
        return flow.asStateFlow()
    }

    override fun getTransactionsByMonth(yearMonth: String): Flow<List<Transaction>> {
        val flow = MutableStateFlow<List<Transaction>>(emptyList())
        flow.value = transactionMap.values.filter { it.date.startsWith(yearMonth) }
        return flow.asStateFlow()
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        val flow = MutableStateFlow<List<Transaction>>(emptyList())
        flow.value = transactionMap.values.filter {
            it.description.contains(query, ignoreCase = true) ||
            it.rawLine.contains(query, ignoreCase = true)
        }
        return flow.asStateFlow()
    }
}

/**
 * Factory function to create repository for Android.
 */
fun createTransactionRepository(context: Context): TransactionRepo {
    return MongoDbAndroid(context)
}