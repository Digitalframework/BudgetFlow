package data

import com.banking.shared.data.Transaction

/**
 * JavaScript/Node.js implementation of TransactionDocument.
 */
actual class TransactionDocument actual constructor(
    actual val _id: String,
    actual val id: String,
    actual val date: String,
    actual val description: String,
    actual val amount: Double,
    actual val category: String,
    actual val rawLine: String,
    actual val createdAt: Long,
    actual val updatedAt: Long
) {
    actual fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            date = date,
            description = description,
            amount = amount,
            category = category,
            rawLine = rawLine
        )
    }

    actual companion object {
        actual fun fromTransaction(transaction: Transaction): TransactionDocument {
            val now = currentTimeMillis()
            return TransactionDocument(
                _id = transaction.id,
                id = transaction.id,
                date = transaction.date,
                description = transaction.description,
                amount = transaction.amount,
                category = transaction.category,
                rawLine = transaction.rawLine,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
