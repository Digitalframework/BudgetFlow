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
import androidx.compose.material3.TextButton
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            .clip(RoundedCornerShape(20.dp)).background(T.surface).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(50))
                .background(hexColor(category?.color).copy(alpha = .13f)),
            contentAlignment = Alignment.Center,
        ) { Text(category?.icon ?: "•", fontSize = 21.sp) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(transaction.description, color = T.text, fontSize = 14.sp,
                fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${fmtDay(transaction.date)} ${transaction.date.take(4)}", color = T.textMuted,
                fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            Box {
                TextButton(onClick = { menuOpen = true }, contentPadding = PaddingValues(0.dp)) {
                    Text("${category?.label ?: transaction.category} ▾", color = T.accent, fontSize = 12.sp)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("${option.icon} ${option.label}") },
                            onClick = { onCategoryChange(option.name); menuOpen = false },
                        )
                    }
                }
            }
        }
        Text(fmtEur(transaction.amount), color = T.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}