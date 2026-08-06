package data

import android.content.Context
import com.banking.shared.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS = "banking_store"
private const val KEY_TRANSACTIONS = "banking_transactions"

/**
 * Local transaction store for Android.
 *
 * MongoDB Atlas Device Sync is still the intended backend; until it is wired up
 * this keeps the data in memory and mirrors it into SharedPreferences, so an
 * import survives a restart the same way the web app's localStorage copy does.
 */
class MongoDbAndroid(context: Context) : TransactionRepo {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val transactionMap = linkedMapOf<String, Transaction>()

    init {
        readPersisted().forEach { transactionMap[it.id] = it }
        publish(persist = false)
    }

    private fun publish(persist: Boolean = true) {
        _transactions.value = transactionMap.values.toList()
        if (persist) writePersisted(transactionMap.values)
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = _transactions.asStateFlow()

    override suspend fun getTransactionById(id: String): Transaction? = transactionMap[id]

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionMap[transaction.id] = transaction
        publish()
    }

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        transactions.forEach { transactionMap[it.id] = it }
        publish()
    }

    override suspend fun updateCategory(id: String, category: String) {
        transactionMap[id]?.let { transactionMap[id] = it.copy(category = category) }
        publish()
    }

    override suspend fun deleteTransaction(id: String) {
        transactionMap.remove(id)
        publish()
    }

    override suspend fun deleteAllTransactions() {
        transactionMap.clear()
        publish()
    }

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        MutableStateFlow(transactionMap.values.filter { it.category == category }).asStateFlow()

    override fun getTransactionsByMonth(yearMonth: String): Flow<List<Transaction>> =
        MutableStateFlow(transactionMap.values.filter { it.date.startsWith(yearMonth) }).asStateFlow()

    override fun searchTransactions(query: String): Flow<List<Transaction>> =
        MutableStateFlow(
            transactionMap.values.filter {
                it.description.contains(query, ignoreCase = true) ||
                    it.rawLine.contains(query, ignoreCase = true)
            }
        ).asStateFlow()

    // ── Persistence ───────────────────────────────────────────────────────────

    private fun readPersisted(): List<Transaction> {
        val raw = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optString("id")
                if (id.isEmpty()) return@mapNotNull null
                Transaction(
                    id = id,
                    date = item.optString("date"),
                    description = item.optString("description"),
                    amount = item.optDouble("amount", 0.0),
                    category = item.optString("category", "Sonstiges"),
                    rawLine = item.optString("rawLine"),
                )
            }
        } catch (e: org.json.JSONException) {
            emptyList()
        }
    }

    private fun writePersisted(transactions: Collection<Transaction>) {
        val array = JSONArray()
        transactions.forEach { tx ->
            array.put(
                JSONObject()
                    .put("id", tx.id)
                    .put("date", tx.date)
                    .put("description", tx.description)
                    .put("amount", tx.amount)
                    .put("category", tx.category)
                    .put("rawLine", tx.rawLine)
            )
        }
        prefs.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
    }
}
