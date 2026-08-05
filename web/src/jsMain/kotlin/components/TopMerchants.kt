package components

import antd.Empty
import antd.Tooltip
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import design.T
import design.fmtEur
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import utils.jso
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.pct

private const val LIMIT = 7

private val NOISE = Regex(
  """\b(gmbh|ag|kg|e\.?k\.?|co\.?kg|n\.?v\.?|sagt danke|bedankt sich)\b""",
  RegexOption.IGNORE_CASE,
)

private fun normalize(description: String): String =
  description
    .replace(NOISE, "")
    .replace(Regex("""\s+"""), " ")
    .trim()

/**
 * Which merchants the money actually goes to — the question the category chart
 * can't answer. Single series, so one hue for every bar.
 */
external interface TopMerchantsProps : Props {
  var transactions: List<Transaction>
  var categories: List<Category>
  var onSelectMerchant: ((String) -> Unit)?
}

@JsName("TopMerchants")
val TopMerchants: FC<TopMerchantsProps> = FC { props ->
  val rows = props.transactions
    .groupBy { normalize(it.description).ifEmpty { "Unbekannt" } }
    .map { (name, list) ->
      MerchantRow(
        name = name,
        total = list.sumOf { it.amount },
        count = list.size,
        category = list.first().category,
      )
    }
    .sortedByDescending { it.total }
    .take(LIMIT)

  val max = rows.firstOrNull()?.total ?: 1.0

  Panel {
    title = "Top-Empfänger"
    extra = if (rows.isNotEmpty()) "Top ${rows.size}" else null
    style = jso { height = 100.pct }

    children = Fragment.create {
      if (rows.isEmpty()) {
        Empty {
          description = "Keine Daten"
          image = null
          style = jso { margin = "16px 0".unsafeCast<web.cssom.Margin>() }
        }
      } else {
        rows.forEach { row ->
          val category = props.categories.find { it.name == row.category }

          Tooltip {
            key = row.name
            placement = "topRight"
            title = "${row.count} × · Ø ${fmtEur(row.total / row.count)} · " +
              (category?.label ?: row.category)
            children = button.create {
              asDynamic().type = "button"
              className = ClassName("bar-row")
              onClick = { props.onSelectMerchant?.invoke(row.name) }

              div {
                className = ClassName("bar-row__top")
                span {
                  className = ClassName("cat-dot")
                  style = jso { background = Color(category?.color ?: T.textMuted) }
                }
                span {
                  className = ClassName("bar-row__name")
                  +row.name
                }
                span {
                  className = ClassName("bar-row__meta")
                  span {
                    className = ClassName("bar-row__share")
                    +"${row.count} ×"
                  }
                  span {
                    className = ClassName("bar-row__value")
                    +fmtEur(row.total)
                  }
                }
              }
              div {
                className = ClassName("bar-track")
                div {
                  className = ClassName("bar-fill")
                  style = jso {
                    width = ((row.total / max) * 100).pct
                    background = Color(T.accent)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private data class MerchantRow(
  val name: String,
  val total: Double,
  val count: Int,
  val category: String,
)
