package com.banking.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import data.Category
import data.Transaction

@Composable
fun CategoryBreakdown(
    transactions: List<Transaction>,
    categories: List<Category>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "No data to display",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val breakdown = remember(transactions) {
                    val total = transactions.sumOf { it.amount }
                    transactions.groupBy(
                        { it.category },
                        { it.amount }
                    ).mapValues { it.value.sum() }
                        .mapValues { it.value / total * 100 }
                }

                // Pie Chart
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                ) {
                    drawPieChart(breakdown, categories)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    breakdown.entries.sortedByDescending { it.value }.forEach { (category, percentage) ->
                        val cat = categories.find { it.name == category }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .wrapContentSize()
                                ) {
                                    Canvas(modifier = Modifier.size(16.dp)) {
                                        drawCircle(
                                            color = Color(android.graphics.Color.parseColor(cat?.color ?: "#8c8c8c")),
                                            radius = 8.dp.toPx()
                                        )
                                    }
                                }
                                Text(
                                    text = "${cat?.icon ?: "❓"} ${cat?.label ?: category}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "%.1f%%".format(percentage),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPieChart(
    breakdown: Map<String, Double>,
    categories: List<Category>
) {
    if (breakdown.isEmpty()) return

    val sortedEntries = breakdown.entries.sortedByDescending { it.value }
    var startAngle = 0f

    sortedEntries.forEach { (category, percentage) ->
        val cat = categories.find { it.name == category }
        val sweepAngle = (percentage / 100 * 360).toFloat()
        val color = Color(android.graphics.Color.parseColor(cat?.color ?: "#8c8c8c"))

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height)
        )

        startAngle += sweepAngle
    }
}