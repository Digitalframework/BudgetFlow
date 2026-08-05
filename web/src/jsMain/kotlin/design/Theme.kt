package design

import antd.theme as antdTheming
import utils.jso

// ── Design tokens ─────────────────────────────────────────────────────────────
// Single dark surface set. Chart ink / grid values are chosen for >= 3:1 against
// `surface`, so bars and labels stay readable without borders.
// Kept in sync with the CSS custom properties in resources/index.html.
object T {
  const val bg = "#0e0e11"
  const val surface = "#17171b"
  const val surfaceAlt = "#1d1d23"
  const val surfaceHover = "#23232a"
  const val border = "rgba(255,255,255,0.07)"
  const val borderStrong = "rgba(255,255,255,0.14)"

  const val text = "#f4f4f3"
  const val textSecondary = "#a9a8a2"
  const val textMuted = "#77766f"

  const val accent = "#3987e5"
  const val accentSoft = "rgba(57,135,229,0.14)"
  const val track = "#24242b"
  const val grid = "#26262b"

  const val good = "#0ca30c"
  const val critical = "#d03b3b"

  const val mono = "'DM Mono', ui-monospace, SFMono-Regular, monospace"
  const val sans = "'Inter', system-ui, -apple-system, 'Segoe UI', sans-serif"
}

/** ConfigProvider theme: dark algorithm + our tokens, mirroring theme.js. */
val antdTheme: dynamic
  get() {
    // Must be read outside the jso block: inside it the receiver is `dynamic`,
    // so a package-qualified `antd.theme` compiles to a lookup on the receiver.
    val darkAlgorithm = antdTheming.darkAlgorithm

    return jso<dynamic> {
      algorithm = darkAlgorithm
      token = jso<dynamic> {
        colorPrimary = T.accent
        colorBgBase = T.bg
        colorBgContainer = T.surface
        colorBgElevated = T.surfaceAlt
        colorBorder = T.borderStrong
        colorBorderSecondary = T.border
        colorText = T.text
        colorTextSecondary = T.textSecondary
        colorTextTertiary = T.textMuted
        borderRadius = 10
        fontFamily = T.sans
        fontSize = 14
      }
      components = jso<dynamic> {
        Layout = jso<dynamic> {
          headerBg = T.bg
          bodyBg = T.bg
          siderBg = T.bg
          headerHeight = 60
        }
        Table = jso<dynamic> {
          headerBg = T.surface
          headerColor = T.textMuted
          headerSplitColor = "transparent"
          rowHoverBg = T.surfaceAlt
          borderColor = T.border
          cellPaddingBlockSM = 10
        }
        Segmented = jso<dynamic> {
          itemSelectedBg = T.accent
          itemSelectedColor = "#fff"
          trackBg = T.surfaceAlt
        }
        Select = jso<dynamic> { optionSelectedBg = T.surfaceHover }
        Tooltip = jso<dynamic> {
          colorBgSpotlight = T.surfaceHover
          colorTextLightSolid = T.text
        }
      }
    }
  }

// ── Formatting helpers ────────────────────────────────────────────────────────

private fun createEurFormat(): dynamic =
  js("new Intl.NumberFormat('de-DE',{style:'currency',currency:'EUR',minimumFractionDigits:2})")

private fun createEurShortFormat(): dynamic =
  js("new Intl.NumberFormat('de-DE',{style:'currency',currency:'EUR',maximumFractionDigits:0})")

private val eur = createEurFormat()
private val eur0 = createEurShortFormat()

fun fmtEur(n: Double?): String = eur.format(n ?: 0.0) as String

fun fmtEurShort(n: Double?): String = eur0.format(n ?: 0.0) as String

/** 0.184 → "18 %", 0.043 → "4.3 %" */
fun fmtPct(n: Double): String {
  val digits = if (n < 0.1) 1 else 0
  return "${(n * 100).asDynamic().toFixed(digits)} %"
}

fun fmtFixed(n: Double, digits: Int = 0): String = n.asDynamic().toFixed(digits) as String

private val MONTHS = arrayOf(
  "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
  "Jul", "Aug", "Sep", "Okt", "Nov", "Dez",
)

/** "2026-04" → "Apr 2026" */
fun fmtMonth(ym: String?): String {
  if (ym.isNullOrEmpty()) return ""
  val parts = ym.split("-")
  if (parts.size < 2) return ym
  val m = parts[1].toIntOrNull()
  val name = if (m != null && m in 1..12) MONTHS[m - 1] else parts[1]
  return "$name ${parts[0]}"
}

/** "2026-04-13" → "13. Apr" */
fun fmtDay(iso: String?): String {
  if (iso.isNullOrEmpty()) return ""
  val parts = iso.split("-")
  if (parts.size < 3) return iso
  val m = parts[1].toIntOrNull()
  val name = if (m != null && m in 1..12) MONTHS[m - 1] else parts[1]
  val d = parts[2].toIntOrNull() ?: parts[2]
  return "$d. $name"
}
