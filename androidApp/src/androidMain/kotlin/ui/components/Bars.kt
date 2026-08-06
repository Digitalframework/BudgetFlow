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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.theme.T

/** Category swatch — identity next to the label, never the only channel. */
@Composable
fun CatDot(color: Color, size: androidx.compose.ui.unit.Dp = 9.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

/** Magnitude bar: 0f..1f of the widest row in the same chart. */
@Composable
fun BarTrack(
    fraction: Float,
    color: Color = T.accent,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 6.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(T.track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .widthIn(min = 3.dp)
                .height(height)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
    }
}

/**
 * One row of a ranked magnitude chart: swatch + name on the left, share and
 * value on the right, bar underneath. Tapping filters, exactly like the web.
 */
@Composable
fun BarRow(
    name: String,
    value: String,
    share: String,
    fraction: Float,
    modifier: Modifier = Modifier,
    dotColor: Color = T.textMuted,
    barColor: Color = T.accent,
    selected: Boolean = false,
    dimmed: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) T.surfaceAlt else Color.Transparent)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CatDot(dotColor)
            Text(
                text = name,
                fontSize = 13.sp,
                color = T.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = share,
                fontSize = 12.sp,
                color = T.textMuted,
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = T.text,
                modifier = Modifier.padding(start = 2.dp),
            )
        }

        Box(modifier = Modifier.padding(top = 7.dp)) {
            BarTrack(
                fraction = fraction,
                color = if (dimmed) barColor.copy(alpha = 0.4f) else barColor,
            )
        }
    }
}
