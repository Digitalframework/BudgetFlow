package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter

/**
 * Drawer content: search, period and category, mirroring the web sider. The
 * category counts respect the month + search filters, so the list reflects what
 * a category tap would actually show.
 */
@Composable
fun SidebarFilters(
    transactions: List<Transaction>,
    categories: List<Category>,
    filter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val months = transactions
        .map { it.date.take(7) }
        .filter { it.isNotEmpty() }
        .distinct()
        .sortedDescending()

    val counts = transactions
        .filter { filter.month == null || it.date.startsWith(filter.month!!) }
        .filter {
            filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true)
        }
        .groupingBy { it.category }
        .eachCount()

    val dirty = filter.category != "all" || filter.month != null || filter.search.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(T.bg)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Konto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = T.text,
            )
            Text(
                text = "Analyse",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = T.accent,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Filter schließen", tint = T.textMuted)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(T.border),
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Suche ────────────────────────────────────────────────────────
            Column {
                FilterLabel("Suche")
                OutlinedTextField(
                    value = filter.search,
                    onValueChange = { onFilterChange(filter.copy(search = it)) },
                    placeholder = { Text("Beschreibung…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = T.textMuted)
                    },
                    trailingIcon = {
                        if (filter.search.isNotEmpty()) {
                            IconButton(onClick = { onFilterChange(filter.copy(search = "")) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Suche leeren",
                                    tint = T.textMuted,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Zeitraum ─────────────────────────────────────────────────────
            Column {
                FilterLabel("Zeitraum")
                MonthPicker(
                    months = months,
                    selected = filter.month,
                    onSelect = { onFilterChange(filter.copy(month = it)) },
                )
            }

            // ── Kategorie ────────────────────────────────────────────────────
            Column {
                FilterLabel("Kategorie")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CategoryItem(
                        label = "Alle Kategorien",
                        dotColor = T.textMuted.copy(alpha = 0.5f),
                        count = counts.values.sum(),
                        active = filter.category == "all",
                        onClick = { onFilterChange(filter.copy(category = "all")) },
                    )
                    categories.forEach { category ->
                        val isActive = filter.category == category.name
                        val count = counts[category.name] ?: 0
                        CategoryItem(
                            label = "${category.icon} ${category.label}",
                            dotColor = hexColor(category.color),
                            count = count,
                            active = isActive,
                            faded = count == 0 && !isActive,
                            onClick = {
                                onFilterChange(
                                    filter.copy(category = if (isActive) "all" else category.name)
                                )
                            },
                        )
                    }
                }
            }

            if (dirty) {
                TextButton(
                    onClick = {
                        onFilterChange(TransactionFilter(category = "all", month = null, search = ""))
                    },
                ) {
                    Text("Filter zurücksetzen", color = T.textSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(
    months: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(T.surfaceAlt)
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selected?.let { fmtMonth(it) } ?: "Alle Monate",
                fontSize = 14.sp,
                color = T.text,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = T.textMuted,
            )
        }

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Alle Monate") },
                onClick = { onSelect(null); open = false },
            )
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(fmtMonth(month)) },
                    onClick = { onSelect(month); open = false },
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    label: String,
    dotColor: androidx.compose.ui.graphics.Color,
    count: Int,
    active: Boolean,
    faded: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) T.surfaceHover else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        CatDot(if (faded) dotColor.copy(alpha = 0.45f) else dotColor)
        Text(
            text = label,
            fontSize = 13.sp,
            color = when {
                active -> T.text
                faded -> T.textMuted
                else -> T.textSecondary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Pill(text = "$count", active = active)
    }
}
