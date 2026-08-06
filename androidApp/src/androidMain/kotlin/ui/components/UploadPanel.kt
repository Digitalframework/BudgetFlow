package com.banking.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.theme.T

/**
 * `compact` renders just the import action for the app bar; the full dropzone is
 * the empty state, so it stops eating half the screen once data exists.
 */
@Composable
fun UploadPanel(
    onUpload: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(onUpload) }

    val pick = { launcher.launch("application/pdf") }

    if (compact) {
        IconButton(onClick = { pick() }, modifier = modifier) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "PDF importieren",
                tint = T.textSecondary,
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(T.surface)
            .drawBehind {
                drawRoundRect(
                    color = T.borderStrong,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    ),
                )
            }
            .clickable { pick() }
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = T.accent,
            modifier = Modifier
                .size(38.dp)
                .padding(bottom = 8.dp),
        )
        Text(
            text = "Kontoauszug als PDF auswählen",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = T.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Sparkasse, ING, DKB, Commerzbank, Volksbank & mehr.\n" +
                "Die Auswertung passiert lokal auf dem Gerät.",
            fontSize = 13.sp,
            color = T.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Button(onClick = { pick() }) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 2.dp),
            )
            Text(text = "PDF auswählen", modifier = Modifier.padding(start = 6.dp))
        }
    }
}
