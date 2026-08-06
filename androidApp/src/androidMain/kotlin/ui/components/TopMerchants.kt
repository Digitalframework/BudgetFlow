package com.banking.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction

private const val LIMIT = 7

private val NOISE = Regex(
    """\b(gmbh|ag|kg|e\.?k\.?|co\.?kg|n\.?v\.?|sagt danke|bedankt sich)\b""",
    RegexOption.IGNORE_CASE,
)

private fun normalize(description: String): String =
    description
        .replace(NOISE, "")
        .replace(Regex("""\s+"""), " ")
        .trim()

private data class MerchantRow(
    val name: String,
    val total: Double,
    val count: Int,
    val category: String,
)

/**
 * Which merchants the money actually goes to — the question the category chart
 * can't answer. Single series, so one hue for every bar. Tapping a row puts the
 * merchant into the search filter.
 */
@Composable
fun TopMerchants(
    transactions: List<Transaction>,
    categories: List<Category>,
    onSelectMerchant: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = transactions
        .groupBy { normalize(it.description).ifEmpty { "Unbekannt" } }
        .map { (name, list) ->
            MerchantRow(
                name = name,
                total = list.sumOf { it.amount },
                count = list.size,
                category = list.first().category,
            )
        }
        .sortedByDescending { it.total }
        .take(LIMIT)

    val max = rows.firstOrNull()?.total ?: 1.0

    Panel(
        modifier = modifier,
        title = "Top-Empfänger",
        extra = if (rows.isNotEmpty()) "Top ${rows.size}" else null,
        bodyPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    ) {
        if (rows.isEmpty()) {
            EmptyHint(modifier = Modifier.padding(horizontal = 10.dp))
        } else {
            rows.forEach { row ->
                val category = categories.find { it.name == row.category }
                BarRow(
                    name = row.name,
                    value = fmtEur(row.total),
                    share = "${row.count} ×",
                    fraction = (row.total / max).toFloat(),
                    dotColor = hexColor(category?.color),
                    barColor = T.accent,
                    onClick = { onSelectMerchant(row.name) },
                )
            }
        }
    }
}
