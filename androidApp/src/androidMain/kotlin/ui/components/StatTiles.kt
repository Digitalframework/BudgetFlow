package com.banking.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.format.fmtFixed
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction

@Composable
private fun Tile(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
    dot: Color? = null,
) {
    Panel(
        modifier = modifier,
        bodyPadding = PaddingValues(18.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = T.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (dot != null) CatDot(dot, size = 11.dp)
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = T.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = hint,
            fontSize = 12.sp,
            color = T.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * The four headline numbers of the overview. Web lays them out as one row of
 * four; on a phone they read better as a 2 × 2 grid.
 */
@Composable
fun StatTiles(
    transactions: List<Transaction>,
    categories: List<Category>,
    modifier: Modifier = Modifier,
) {
    val total = transactions.sumOf { it.amount }
    val months = transactions
        .map { it.date.take(7) }
        .filter { it.isNotEmpty() }
        .distinct()
        .sorted()

    val byCategory = transactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
    val top = byCategory.entries.maxByOrNull { it.value }

    val count = transactions.size
    val avg = if (count > 0) total / count else 0.0
    val perMonth = if (months.isNotEmpty()) total / months.size else 0.0

    val period = when {
        months.isEmpty() -> "keine Daten"
        months.size == 1 -> fmtMonth(months.first())
        else -> "${fmtMonth(months.first())} – ${fmtMonth(months.last())}"
    }

    val topCategory = top?.let { entry -> categories.find { it.name == entry.key } }
    val topShare = if (total != 0.0 && top != null) (top.value / total) * 100 else 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TileCell {
                Tile(
                    label = "Gesamtausgaben",
                    value = fmtEur(total),
                    hint = period,
                )
            }
            TileCell {
                Tile(
                    label = "Buchungen",
                    value = "$count",
                    hint = "Ø ${fmtEur(avg)} pro Buchung",
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TileCell {
                Tile(
                    label = "Ø pro Monat",
                    value = fmtEur(perMonth),
                    hint = if (months.isNotEmpty()) {
                        "über ${months.size} ${if (months.size == 1) "Monat" else "Monate"}"
                    } else "",
                )
            }
            TileCell {
                Tile(
                    label = "Größter Posten",
                    value = topCategory?.label ?: "–",
                    dot = topCategory?.let { hexColor(it.color) },
                    hint = if (topCategory != null && top != null) {
                        "${fmtEur(top.value)} · ${fmtFixed(topShare)} % der Ausgaben"
                    } else "",
                )
            }
        }
    }
}

@Composable
private fun RowScope.TileCell(content: @Composable () -> Unit) {
    Column(modifier = Modifier.weight(1f)) { content() }
}
