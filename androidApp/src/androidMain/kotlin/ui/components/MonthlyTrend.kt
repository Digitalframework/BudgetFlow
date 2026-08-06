package com.banking.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.format.fmtEurShort
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.shared.data.Transaction

private val PLOT_HEIGHT = 132.dp

/** A column of the trend chart; months without a booking still get an entry. */
private data class MonthBar(val ym: String, val total: Double, val count: Int)

/**
 * Change over time, one series → one hue. Months with no booking still appear so
 * gaps read as gaps rather than being squeezed out. The chart doubles as the
 * month picker.
 */
@Composable
fun MonthlyTrend(
    transactions: List<Transaction>,
    activeMonth: String?,
    onSelectMonth: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val byMonth = transactions
        .filter { it.date.length >= 7 }
        .groupBy { it.date.take(7) }

    val keys = byMonth.keys.sorted()
    // fill the calendar gaps between the first and the last month
    val months = if (keys.isEmpty()) emptyList() else buildList {
        val first = keys.first().split("-")
        val last = keys.last().split("-")
        var y = first[0].toInt()
        var m = first[1].toInt()
        val endY = last[0].toInt()
        val endM = last[1].toInt()
        while (y * 12 + m <= endY * 12 + endM) {
            val ym = "$y-${m.toString().padStart(2, '0')}"
            val entries = byMonth[ym].orEmpty()
            add(MonthBar(ym, entries.sumOf { it.amount }, entries.size))
            if (m == 12) {
                y += 1; m = 1
            } else {
                m += 1
            }
        }
    }

    val max = months.maxOfOrNull { it.total }?.takeIf { it > 0.0 } ?: 1.0
    val avg = if (months.isNotEmpty()) months.sumOf { it.total } / months.size else 0.0
    // Value labels only fit on a phone while the columns stay wide.
    val showLabels = months.size <= 5
    val plotArea = PLOT_HEIGHT - (if (showLabels) 22.dp else 0.dp)
    // Past six months the columns get too narrow to read, so scroll instead.
    val scrolls = months.size > 6

    Panel(
        modifier = modifier,
        title = "Monatlicher Verlauf",
        extra = if (months.size > 1) "Ø ${fmtEurShort(avg)} / Monat" else null,
    ) {
        if (months.isEmpty()) {
            EmptyHint()
        } else {
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (scrolls) it.horizontalScroll(scrollState) else it },
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .height(PLOT_HEIGHT)
                            .let { if (scrolls) it else it.fillMaxWidth() },
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        months.forEach { bar ->
                            MonthColumn(
                                bar = bar,
                                isActive = activeMonth == bar.ym,
                                anyActive = activeMonth != null,
                                barHeight = (plotArea * (bar.total / max).toFloat())
                                    .coerceAtLeast(3.dp),
                                showLabel = showLabels,
                                onClick = {
                                    onSelectMonth(if (activeMonth == bar.ym) null else bar.ym)
                                },
                                modifier = if (scrolls) Modifier.width(52.dp) else Modifier.weight(1f),
                            )
                        }
                    }

                    // Baseline + average reference line.
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(T.grid),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .let { if (scrolls) it else it.fillMaxWidth() },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        months.forEach { bar ->
                            val isActive = activeMonth == bar.ym
                            Text(
                                text = fmtMonth(bar.ym),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isActive) T.text else T.textMuted,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = if (scrolls) Modifier.width(52.dp) else Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Only meaningful while every column is on screen at once.
                if (months.size > 1 && !scrolls) {
                    AverageLine(
                        fraction = (avg / max).toFloat(),
                        plotArea = plotArea,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PLOT_HEIGHT),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthColumn(
    bar: MonthBar,
    isActive: Boolean,
    anyActive: Boolean,
    barHeight: Dp,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showLabel) {
            Text(
                text = if (bar.total > 0.0) fmtEurShort(bar.total) else "",
                fontSize = 11.sp,
                maxLines = 1,
                color = if (isActive) T.text else T.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 46.dp)
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    if (!anyActive || isActive) T.accent else T.accent.copy(alpha = 0.35f)
                ),
        )
    }
}

/** Dashed mean marker across the plot, drawn above the columns. */
@Composable
private fun AverageLine(fraction: Float, plotArea: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val y = size.height - plotArea.toPx() * fraction.coerceIn(0f, 1f)
        drawLine(
            color = T.grid,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )
    }
}
