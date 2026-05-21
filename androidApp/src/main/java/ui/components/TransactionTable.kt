package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction

@Composable
fun TransactionTable(
    transactions: List<Transaction>,
    categories: List<Category>,
    onCategoryChange: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions found. Upload a PDF to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            categories = categories,
                            onCategoryChange = { newCategory ->
                                onCategoryChange(transaction.id, newCategory)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    categories: List<Category>,
    onCategoryChange: (String) -> Unit
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val currentCategory = categories.find { it.name == transaction.category }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date and Description
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }

        // Amount
        Text(
            text = "-€${"%.2f".format(transaction.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        // Category Selector
        Box {
            AssistChip(
                onClick = { categoryMenuExpanded = true },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = currentCategory?.icon ?: "❓")
                        Text(text = currentCategory?.label ?: transaction.category)
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(126).copy(alpha = 0.2f) //currentCategory?.color ?:
                )
            )

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = category.icon)
                                Text(text = category.label)
                            }
                        },
                        onClick = {
                            onCategoryChange(category.name)
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}