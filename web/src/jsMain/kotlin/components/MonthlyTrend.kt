package components

import antd.Empty
import antd.Tooltip
import com.banking.shared.data.Transaction
import design.T
import design.fmtEur
import design.fmtEurShort
import design.fmtMonth
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import utils.jso
import web.cssom.AlignItems
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontFamily
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.TextOverflow
import web.cssom.pct
import web.cssom.px

private const val PLOT_HEIGHT = 132.0

/**
 * Change over time, one series → one hue. Months with no booking still appear so
 * gaps read as gaps rather than being squeezed out. The chart doubles as the
 * month picker.
 */
external interface MonthlyTrendProps : Props {
  var transactions: List<Transaction>
  var activeMonth: String?
  var onSelectMonth: ((String?) -> Unit)?
}

@JsName("MonthlyTrend")
val MonthlyTrend: FC<MonthlyTrendProps> = FC { props ->
  val byMonth = props.transactions
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
  val showLabels = months.size <= 9
  val plotArea = PLOT_HEIGHT - (if (showLabels) 24.0 else 0.0)

  Panel {
    title = "Monatlicher Verlauf"
    extra = if (months.size > 1) "Ø ${fmtEurShort(avg)} / Monat" else null
    style = jso { height = 100.pct }

    children = Fragment.create {
      if (months.isEmpty()) {
        Empty {
          description = "Keine Daten"
          image = null
          style = jso { margin = "16px 0".unsafeCast<web.cssom.Margin>() }
        }
      } else {
        div {
          style = jso { paddingTop = 4.px }

          // ── Plot ───────────────────────────────────────────────────────────
          div {
            style = jso {
              position = Position.relative
              display = Display.flex
              alignItems = AlignItems.flexEnd
              gap = 10.px
              height = PLOT_HEIGHT.px
              borderBottom = "1px solid ${T.grid}".unsafeCast<web.cssom.BorderBottom>()
            }

            // average reference line
            if (months.size > 1) {
              div {
                style = jso {
                  position = Position.absolute
                  left = 0.px
                  right = 0.px
                  bottom = ((avg / max) * plotArea).px
                  borderTop = "1px dashed ${T.grid}".unsafeCast<web.cssom.BorderTop>()
                  pointerEvents = "none".unsafeCast<web.cssom.PointerEvents>()
                }
              }
            }

            months.forEach { bar ->
              val isActive = props.activeMonth == bar.ym

              Tooltip {
                key = bar.ym
                title = "${fmtMonth(bar.ym)} · ${fmtEur(bar.total)} · ${bar.count} " +
                  (if (bar.count == 1) "Buchung" else "Buchungen")
                children = button.create {
                  asDynamic().type = "button"
                  className = ClassName("month-col")
                  asDynamic()["aria-pressed"] = isActive
                  style = jso {
                    justifyContent = JustifyContent.flexEnd
                    height = 100.pct
                  }
                  onClick = { props.onSelectMonth?.invoke(if (isActive) null else bar.ym) }

                  if (showLabels) {
                    span {
                      style = jso {
                        fontSize = 11.px
                        fontFamily = T.mono.unsafeCast<FontFamily>()
                        color = Color(if (isActive) T.text else T.textSecondary)
                      }
                      +(if (bar.total > 0.0) fmtEurShort(bar.total) else "")
                    }
                  }
                  span {
                    className = ClassName("month-col__bar")
                    style = jso {
                      // px, not %, so the value label above always has room
                      height = maxOf((bar.total / max) * plotArea, 3.0).px
                      opacity = (if (props.activeMonth == null || isActive) 1.0 else 0.35)
                        .unsafeCast<web.cssom.Opacity>()
                    }
                  }
                }
              }
            }
          }

          // ── Axis labels ────────────────────────────────────────────────────
          div {
            style = jso {
              display = Display.flex
              gap = 10.px
              marginTop = 8.px
            }
            months.forEach { bar ->
              val isActive = props.activeMonth == bar.ym
              div {
                key = bar.ym
                className = ClassName("month-col__label")
                style = jso {
                  flex = "1 1 0".unsafeCast<web.cssom.Flex>()
                  minWidth = 0.px
                  textAlign = TextAlign.center
                  color = Color(if (isActive) T.text else T.textMuted)
                  fontWeight = (if (isActive) 600 else 400).unsafeCast<FontWeight>()
                  overflow = Overflow.hidden
                  textOverflow = TextOverflow.ellipsis
                }
                +fmtMonth(bar.ym)
              }
            }
          }
        }
      }
    }
  }
}

private data class MonthBar(val ym: String, val total: Double, val count: Int)
