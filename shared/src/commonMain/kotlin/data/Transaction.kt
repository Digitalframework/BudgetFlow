package com.banking.shared.data

/**
 * Represents a bank transaction.
 */
data class Transaction(
    val id: String,
    val date: String,
    val description: String,
    val amount: Double,
    val category: String = "Sonstiges",
    val rawLine: String = ""
)

/**
 * Represents a transaction category with metadata.
 */
data class Category(
    val name: String,
    val label: String,
    val color: String,
    val icon: String,
    val keywords: List<String>
)

/**
 * Filter options for transactions.
 */
data class TransactionFilter(
    val category: String = "all",
    val month: String? = null,
    val search: String = ""
)

/**
 * UI state for the transaction screen.
 */
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val loading: Boolean = false,
    val filter: TransactionFilter = TransactionFilter(),
    val pdfFileName: String? = null
)