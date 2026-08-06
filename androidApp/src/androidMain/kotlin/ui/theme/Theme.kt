package com.banking.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Design tokens, kept in sync with the web app (`web/src/jsMain/kotlin/design/Theme.kt`
 * and the CSS custom properties in `resources/index.html`).
 *
 * A single dark surface set — the web app has no light mode either, so following
 * the system theme here would put the two clients on different palettes.
 */
object T {
    val bg = Color(0xFF0E0E11)
    val surface = Color(0xFF17171B)
    val surfaceAlt = Color(0xFF1D1D23)
    val surfaceHover = Color(0xFF23232A)
    val border = Color(0x12FFFFFF)
    val borderStrong = Color(0x24FFFFFF)

    val text = Color(0xFFF4F4F3)
    val textSecondary = Color(0xFFA9A8A2)
    val textMuted = Color(0xFF77766F)

    val accent = Color(0xFF3987E5)
    val accentSoft = Color(0x243987E5)
    val track = Color(0xFF24242B)
    val grid = Color(0xFF26262B)

    val good = Color(0xFF0CA30C)
    val warn = Color(0xFFC98500)
    val critical = Color(0xFFD03B3B)
}

/** `#199e70` → Compose colour. Category colours arrive as CSS hex from shared code. */
fun hexColor(hex: String?): Color = try {
    Color(android.graphics.Color.parseColor(hex ?: "#7b7a74"))
} catch (e: IllegalArgumentException) {
    T.textMuted
}

private val DarkColorScheme = darkColorScheme(
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
