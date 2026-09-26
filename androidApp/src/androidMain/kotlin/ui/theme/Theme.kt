package com.banking.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Light mobile palette with indigo accents and quiet neutral surfaces. */
object T {
    val bg = Color(0xFFF5F6FC)
    val surface = Color(0xFFFFFFFF)
    val surfaceAlt = Color(0xFFEEEFFC)
    val surfaceHover = Color(0xFFE4E7F5)
    val border = Color(0xFFECEEF6)
    val borderStrong = Color(0xFFD8DCEE)

    val text = Color(0xFF19234B)
    val textSecondary = Color(0xFF626C88)
    val textMuted = Color(0xFF747D96)

    val accent = Color(0xFF424BD1)
    val accentSoft = Color(0xFFE9EBFF)
    val track = Color(0xFFE8EBF6)
    val grid = Color(0xFFE8EBF6)

    val good = Color(0xFF16866B)
    val warn = Color(0xFFC98500)
    val critical = Color(0xFFD03B3B)
}

/** `#199e70` → Compose colour. Category colours arrive as CSS hex from shared code. */
fun hexColor(hex: String?): Color = try {
    Color(android.graphics.Color.parseColor(hex ?: "#7b7a74"))
} catch (e: IllegalArgumentException) {
    T.textMuted
}

private val LightColorScheme = lightColorScheme(
    primary = T.accent,
    onPrimary = Color.White,
    primaryContainer = T.surfaceAlt,
    onPrimaryContainer = T.text,
    secondary = T.accent,
    onSecondary = Color.White,
    background = T.bg,
    onBackground = T.text,
    surface = T.surface,
    onSurface = T.text,
    surfaceVariant = T.surfaceAlt,
    onSurfaceVariant = T.textSecondary,
    outline = T.borderStrong,
    outlineVariant = T.border,
    error = T.critical,
    onError = Color.White,
    scrim = Color(0xCC000000),
)

@Composable
fun BankingAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = T.bg.toArgb()
            window.navigationBarColor = T.bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
