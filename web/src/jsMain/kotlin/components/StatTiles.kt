package components

import antd.Col
import antd.Row
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import design.T
import design.fmtEur
import design.fmtFixed
import design.fmtMonth
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import utils.jso
import web.cssom.AlignItems
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontFamily
import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.WhiteSpace
import web.cssom.px

// ─── Tile ─────────────────────────────────────────────────────────────────────

external interface TileProps : Props {
  var label: String
  var value: String
  var hint: String?
  var dot: String?
  var mono: Boolean?
}

@JsName("Tile")
val Tile: FC<TileProps> = FC { props ->
  Panel {
    bodyStyle = jso { padding = 18.px }
    children = Fragment.create {
      div {
        className = ClassName("tile-label")
        +props.label
      }
      div {
        className = ClassName("tile-value")
        style = jso {
          if (props.mono != false) fontFamily = T.mono.unsafeCast<FontFamily>()
          display = Display.flex
          alignItems = AlignItems.center
          gap = 9.px
        }
        props.dot?.let { dotColor ->
          span {
            className = ClassName("cat-dot")
            style = jso {
              background = Color(dotColor)
              width = 11.px
              height = 11.px
            }
          }
        }
        span {
          style = jso {
            overflow = Overflow.hidden
            textOverflow = TextOverflow.ellipsis
            whiteSpace = WhiteSpace.nowrap
          }
          +props.value
        }
      }
      div {
        className = ClassName("tile-hint")
        +(props.hint?.takeIf { it.isNotEmpty() } ?: " ")
      }
    }
  }
}

// ─── StatTiles ────────────────────────────────────────────────────────────────

external interface StatTilesProps : Props {
  var transactions: List<Transaction>
  var categories: List<Category>
}

@JsName("StatTiles")
val StatTiles: FC<StatTilesProps> = FC { props ->
  val transactions = props.transactions

  val total = transactions.sumOf { it.amount }
  val months = transactions
    .map { it.date.take(7) }
    .filter { it.isNotEmpty() }
    .distinct()
    .sorted()

  val byCategory = transactions
    .groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { it.amount } }
  val top = byCategory.entries.maxByOrNull { it.value }

  val count = transactions.size
  val avg = if (count > 0) total / count else 0.0
  val perMonth = if (months.isNotEmpty()) total / months.size else 0.0

  val period = when {
    months.isEmpty() -> "keine Daten"
    months.size == 1 -> fmtMonth(months.first())
    else -> "${fmtMonth(months.first())} – ${fmtMonth(months.last())}"
  }

  val topCategory = top?.let { entry -> props.categories.find { it.name == entry.key } }
  val topShare = if (total != 0.0 && top != null) (top.value / total) * 100 else 0.0

  Row {
    gutter = arrayOf(14, 14)
    children = Fragment.create {
      Col {
        xs = 24; sm = 12; xl = 6
        children = Fragment.create {
          Tile {
            label = "Gesamtausgaben"
            value = fmtEur(total)
            hint = period
          }
        }
      }
      Col {
        xs = 24; sm = 12; xl = 6
        children = Fragment.create {
          Tile {
            label = "Buchungen"
            value = "$count"
            hint = "Ø ${fmtEur(avg)} pro Buchung"
          }
        }
      }
      Col {
        xs = 24; sm = 12; xl = 6
        children = Fragment.create {
          Tile {
            label = "Ø pro Monat"
            value = fmtEur(perMonth)
            hint = if (months.isNotEmpty()) {
              "über ${months.size} ${if (months.size == 1) "Monat" else "Monate"}"
            } else ""
          }
        }
      }
      Col {
        xs = 24; sm = 12; xl = 6
        children = Fragment.create {
          Tile {
            label = "Größter Posten"
            value = topCategory?.label ?: "–"
            mono = false
            dot = topCategory?.color
            hint = if (topCategory != null && top != null) {
              "${fmtEur(top.value)} · ${fmtFixed(topShare)} % der Ausgaben"
            } else ""
          }
        }
      }
    }
  }
}
