package com.banking.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.format.fmtPct
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction

private data class CategoryRow(
    val key: String,
    val total: Double,
    val count: Int,
    val label: String,
    val color: String,
)

/**
 * Ranked magnitude chart: one series (spend), so every bar gets one hue. The
 * category swatch beside each label carries identity, the label carries the
 * name — colour is never the only channel. Tapping a row filters.
 */
@Composable
fun CategoryBreakdown(
    transactions: List<Transaction>,
    categories: List<Category>,
    activeCategory: String?,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = activeCategory ?: "all"

    val rows = transactions
        .groupBy { it.category }
        .map { (key, list) ->
            val category = categories.find { it.name == key }
            CategoryRow(
                key = key,
                total = list.sumOf { it.amount },
                count = list.size,
                label = category?.label ?: key,
                color = category?.color ?: "#7b7a74",
            )
        }
        .sortedByDescending { it.total }

    val sum = rows.sumOf { it.total }
    val max = rows.firstOrNull()?.total ?: 1.0

    Panel(
        modifier = modifier,
        title = "Ausgaben nach Kategorie",
        extra = if (rows.isNotEmpty()) {
            "${rows.size}/${categories.size} · ${fmtEur(sum)}"
        } else null,
        bodyPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    ) {
        if (rows.isEmpty()) {
            EmptyHint(modifier = Modifier.padding(horizontal = 10.dp))
        } else {
            rows.forEach { row ->
                val share = if (sum != 0.0) row.total / sum else 0.0
                val isActive = active == row.key

                BarRow(
                    name = row.label,
                    value = fmtEur(row.total),
                    share = fmtPct(share),
                    fraction = (row.total / max).toFloat(),
                    dotColor = hexColor(row.color),
                    barColor = T.accent,
                    selected = isActive,
                    dimmed = !(isActive || active == "all"),
                    onClick = { onSelectCategory(if (isActive) "all" else row.key) },
                )
            }
        }
    }
}
