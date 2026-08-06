package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.theme.T

/**
 * Flat surface with an optional title row — the Compose counterpart of the web
 * `.panel`, so the whole overview shares one radius, one padding scale, one head
 * style.
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    title: String? = null,
    extra: String? = null,
    bodyPadding: PaddingValues = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(T.surface)
            .border(1.dp, T.border, RoundedCornerShape(14.dp)),
    ) {
        if (title != null || extra != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = T.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (extra != null) {
                    Text(
                        text = extra,
                        fontSize = 12.sp,
                        color = T.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(bodyPadding), content = content)
    }
}

/** Uppercase micro-label used above form fields and list sections. */
@Composable
fun FilterLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
        color = T.textMuted,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

/** The "Keine Daten" placeholder the web app renders with antd's `Empty`. */
@Composable
fun EmptyHint(text: String = "Keine Daten", modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = T.textMuted,
        modifier = modifier.padding(vertical = 16.dp),
    )
}
