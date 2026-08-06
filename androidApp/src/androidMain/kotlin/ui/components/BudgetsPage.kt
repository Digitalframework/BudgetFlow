package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.banking.app.ui.format.fmtDay
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.format.fmtEurShort
import com.banking.app.ui.format.fmtFixed
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import kotlin.math.abs
import kotlin.math.min

private data class BudgetRow(
    val category: Category,
    val limit: Double?,
    val spent: Double,
    val count: Int,
) {
    /** 0..n share of the limit already spent; 0 while no limit is set. */
    val usage: Double get() = limit?.takeIf { it > 0.0 }?.let { spent / it } ?: 0.0
}

private enum class BudgetState(val color: Color) {
    Ok(T.good),
    Tight(T.warn),
    Over(T.critical),
    None(T.textMuted),
}

/**
 * Monthly spending limits per category. The list shows how much of each limit
 * the selected month has already used; picking a row opens the editor, which
 * returns via the arrow on the left. Limits are stored on the device (see
 * `BudgetStore`), so a category without a saved value has to be set up first.
 *
 * [transactions] is unfiltered — the page scopes to [month] itself so the
 * numbers stay comparable.
 */
@Composable
fun BudgetsPage(
    transactions: List<Transaction>,
    categories: List<Category>,
    budgets: Map<String, Double>,
    month: String?,
    onSetBudget: (String, Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }

    val effectiveMonth = month ?: transactions
        .map { it.date.take(7) }
        .filter { it.isNotEmpty() }
        .maxOrNull()

    val monthTx = transactions.filter { effectiveMonth != null && it.date.startsWith(effectiveMonth) }
    val spentByCategory = monthTx
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { abs(it.amount) } }

    val rows = categories.map { category ->
        BudgetRow(
            category = category,
            limit = budgets[category.name],
            spent = spentByCategory[category.name] ?: 0.0,
            count = monthTx.count { it.category == category.name },
        )
    }

    val editingRow = rows.find { it.category.name == editing }

    if (editingRow != null) {
        BudgetEditor(
            row = editingRow,
            month = effectiveMonth,
            monthTx = monthTx,
            draft = draft,
            onDraftChange = { draft = it },
            onBack = { editing = null },
            onSave = { value ->
                onSetBudget(editingRow.category.name, value)
                editing = null
            },
            onRemove = {
                onSetBudget(editingRow.category.name, null)
                editing = null
            },
            modifier = modifier,
        )
        return
    }

    val (budgeted, unbudgeted) = rows.partition { it.limit != null }

    val openEditor: (BudgetRow) -> Unit = { row ->
        draft = row.limit?.let { fmtFixed(it, 0) } ?: ""
        editing = row.category.name
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column {
            Text(
                text = "Budgets",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = T.text,
            )
            Text(
                text = "Setze dir selbst Ausgabenlimits pro Kategorie. Sie werden lokal " +
                    "gespeichert und gegen die Ausgaben des gewählten Monats gerechnet.",
                fontSize = 13.sp,
                color = T.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // Total across every category that has a limit.
        if (budgeted.isNotEmpty()) {
            Panel(
                title = if (effectiveMonth != null) fmtMonth(effectiveMonth) else "Aktueller Monat",
                extra = "${budgeted.size}/${categories.size} Kategorien",
            ) {
                BudgetMeter(
                    spent = budgeted.sumOf { it.spent },
                    limit = budgeted.sumOf { it.limit ?: 0.0 },
                )
            }
        }

        Panel(
            title = "Budgets pro Kategorie",
            extra = if (budgeted.isEmpty()) "Noch kein Budget gesetzt" else null,
            bodyPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
        ) {
            budgeted
                .sortedByDescending { it.usage }
                .forEach { row -> BudgetCard(row = row, onOpen = { openEditor(row) }) }

            if (unbudgeted.isNotEmpty()) {
                FilterLabel(
                    text = "Ohne Budget",
                    modifier = Modifier.padding(
                        start = 10.dp,
                        top = if (budgeted.isEmpty()) 0.dp else 18.dp,
                    ),
                )
                unbudgeted
                    .sortedByDescending { it.spent }
                    .forEach { row -> BudgetCard(row = row, onOpen = { openEditor(row) }) }
            }
        }
    }
}

// ── Editor ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetEditor(
    row: BudgetRow,
    month: String?,
    monthTx: List<Transaction>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: (Double) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parsed = draft.trim().replace(",", ".").toDoubleOrNull()
    val preview = parsed ?: row.limit

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = T.textSecondary)
            Text(text = "Budgets", color = T.textSecondary, modifier = Modifier.padding(start = 6.dp))
        }

        Panel(
            title = "${row.category.icon} ${row.category.label}",
            extra = if (month != null) fmtMonth(month) else "Kein Zeitraum",
        ) {
            FilterLabel("Monatliches Limit")
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text("z. B. 400") },
                trailingIcon = { Text("€", color = T.textSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(50.0, 100.0, 200.0, 400.0, 800.0).forEach { preset ->
                    OutlinedButton(onClick = { onDraftChange(fmtFixed(preset, 0)) }) {
                        Text(fmtEurShort(preset), fontSize = 12.sp)
                    }
                }
            }

            // Live preview against the month that is actually shown.
            Box(modifier = Modifier.padding(top = 14.dp)) {
                BudgetMeter(spent = row.spent, limit = preview)
            }

            Text(
                text = buildString {
                    append(fmtEur(row.spent))
                    append(" ausgegeben · ")
                    append(row.count)
                    append(if (row.count == 1) " Buchung · " else " Buchungen · ")
                    append(preview?.let { "Limit ${fmtEur(it)}" } ?: "kein Limit")
                },
                fontSize = 12.sp,
                color = T.textMuted,
                modifier = Modifier.padding(top = 12.dp),
            )

            Row(
                modifier = Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { parsed?.let { if (it > 0.0) onSave(it) } },
                    enabled = parsed != null && parsed > 0.0,
                ) {
                    Text("Speichern")
                }
                if (row.limit != null) {
                    TextButton(onClick = onRemove) {
                        Text("Budget entfernen", color = T.critical)
                    }
                }
            }
        }

        // The bookings behind the number, so the limit can be judged in context.
        val recent = monthTx
            .filter { it.category == row.category.name }
            .sortedByDescending { abs(it.amount) }
            .take(6)

        Panel(
            title = "Größte Buchungen",
            extra = if (month != null) fmtMonth(month) else null,
        ) {
            if (recent.isEmpty()) {
                EmptyHint("Keine Buchungen in diesem Zeitraum")
            } else {
                recent.forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(fmtDay(tx.date), fontSize = 12.sp, color = T.textMuted)
                        Text(
                            text = tx.description,
                            fontSize = 13.sp,
                            color = T.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = fmtEur(abs(tx.amount)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = T.text,
                        )
                    }
                }
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────

/** One tappable category row: identity on the top, meter across the bottom. */
@Composable
private fun BudgetCard(row: BudgetRow, onOpen: () -> Unit) {
    val color = hexColor(row.category.color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onOpen)
            .padding(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(row.category.icon, fontSize = 15.sp)
            }
            Text(
                text = row.category.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = T.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Pill(text = "${row.count}")
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = T.textMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        Box(modifier = Modifier.padding(top = 8.dp)) {
            if (row.limit == null) {
                Text(
                    text = "Kein Budget · ${fmtEurShort(row.spent)} ausgegeben — jetzt festlegen",
                    fontSize = 12.sp,
                    color = T.textMuted,
                )
            } else {
                BudgetMeter(spent = row.spent, limit = row.limit)
            }
        }
    }
}

/** Rounded count badge, matching `.cat-item__count` on the web. */
@Composable
fun Pill(text: String, active: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(T.track)
            .padding(horizontal = 7.dp, vertical = 1.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (active) T.text else T.textMuted,
        )
    }
}

// ── Meter ─────────────────────────────────────────────────────────────────────

/**
 * Spent-vs-limit bar. Colour marks state (under / close / over) and the text
 * beside it says the same thing, so colour is never the only channel.
 */
@Composable
fun BudgetMeter(spent: Double, limit: Double?, modifier: Modifier = Modifier) {
    val effectiveLimit = limit?.takeIf { it > 0.0 }
    val remaining = effectiveLimit?.let { it - spent }
    val usage = if (effectiveLimit != null) spent / effectiveLimit else 0.0

    val state = when {
        effectiveLimit == null -> BudgetState.None
        remaining!! < 0 -> BudgetState.Over
        usage >= 0.8 -> BudgetState.Tight
        else -> BudgetState.Ok
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (state) {
                    BudgetState.None -> "Kein Limit gesetzt"
                    BudgetState.Over -> "${fmtEurShort(abs(remaining!!))} über Budget"
                    else -> "${fmtEurShort(remaining!!)} verfügbar"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = state.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = if (effectiveLimit != null) {
                    "${fmtEurShort(spent)} / ${fmtEurShort(effectiveLimit)}"
                } else {
                    fmtEurShort(spent)
                },
                fontSize = 13.sp,
                color = T.textSecondary,
                maxLines = 1,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        BarTrack(
            fraction = min(usage, 1.0).toFloat(),
            color = if (effectiveLimit == null) T.track else state.color,
        )
    }
}
