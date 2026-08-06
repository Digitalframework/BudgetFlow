package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.format.fmtDay
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction

/**
 * The booking list. Web renders a paginated antd table with a summary row; on a
 * phone the same information reads better as one scrolling list with the total
 * pinned to the header.
 */
@Composable
fun TransactionTable(
    transactions: List<Transaction>,
    categories: List<Category>,
    onCategoryChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val sorted = remember(transactions) { transactions.sortedByDescending { it.date } }
    val total = transactions.sumOf { it.amount }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${transactions.size} " +
                        if (transactions.size == 1) "Buchung" else "Buchungen",
                    fontSize = 12.sp,
                    color = T.textMuted,
                )
                Text(
                    text = "Summe ${fmtEur(total)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = T.text,
                )
            }
        }

        if (sorted.isEmpty()) {
            item {
                Panel { EmptyHint("Keine Buchungen für diese Auswahl") }
            }
        } else {
            items(sorted, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    categories = categories,
                    onCategoryChange = { onCategoryChange(tx.id, it) },
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    categories: List<Category>,
    onCategoryChange: (String) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val category = categories.find { it.name == transaction.category }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(T.surface)
            .border(1.dp, T.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${fmtDay(transaction.date)} ${transaction.date.take(4)}",
                fontSize = 12.sp,
                color = T.textSecondary,
            )
            Text(
                text = fmtEur(transaction.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = T.text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }

        Text(
            text = transaction.description,
            fontSize = 13.sp,
            color = T.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(T.surfaceAlt)
                    .border(1.dp, T.border, RoundedCornerShape(99.dp))
                    .clickable { menuOpen = true }
                    .padding(start = 7.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(hexColor(category?.color)),
                )
                Text(
                    text = category?.label ?: transaction.category,
                    fontSize = 12.sp,
                    color = T.textSecondary,
                )
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.icon} ${option.label}") },
                        onClick = {
                            onCategoryChange(option.name)
                            menuOpen = false
                        },
                    )
                }
            }
        }
    }
}
