package components

import antd.Button
import antd.ButtonTypeText
import antd.Input
import antd.SearchOutlined
import antd.Select
import antd.SizeSmall
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import design.T
import design.fmtMonth
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import utils.jsObject
import utils.jso
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifySelf
import web.cssom.pct
import web.cssom.px
import web.html.HTMLInputElement

external interface SidebarFiltersProps : Props {
  var transactions: List<Transaction>
  var categories: List<Category>
  var filter: TransactionFilter
  var onFilterChange: (TransactionFilter) -> Unit
}

@JsName("SidebarFilters")
val SidebarFilters: FC<SidebarFiltersProps> = FC { props ->
  val filter = props.filter

  val months = props.transactions
    .map { it.date.take(7) }
    .filter { it.isNotEmpty() }
    .distinct()
    .sortedDescending()

  // Counts respect the month + search filters, so the list reflects what a
  // category click would actually show.
  val counts = props.transactions
    .filter { filter.month == null || it.date.startsWith(filter.month!!) }
    .filter { filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true) }
    .groupingBy { it.category }
    .eachCount()

  val dirty = filter.category != "all" || filter.month != null || filter.search.isNotEmpty()

  val monthOptions = buildList {
    add(jsObject<dynamic> { option -> option.value = "all"; option.label = "Alle Monate" })
    months.forEach { month ->
      add(jsObject<dynamic> { option -> option.value = month; option.label = fmtMonth(month) })
    }
  }.toTypedArray()

  div {
    style = jso {
      padding = "18px 12px".unsafeCast<web.cssom.Padding>()
      display = Display.grid
      gap = 20.px
    }

    // ── Search ────────────────────────────────────────────────────────────────
    div {
      div {
        className = ClassName("filter-label")
        +"Suche"
      }
      Input {
        allowClear = true
        placeholder = "Beschreibung…"
        prefix = SearchOutlined.create()
        value = filter.search
        onChange = { event ->
          val target = event.target.unsafeCast<HTMLInputElement>()
          props.onFilterChange(filter.copy(search = target.value))
        }
      }
    }

    // ── Zeitraum ──────────────────────────────────────────────────────────────
    div {
      div {
        className = ClassName("filter-label")
        +"Zeitraum"
      }
      Select {
        value = filter.month ?: "all"
        style = jso { width = 100.pct }
        options = monthOptions
        onChange = { selected: dynamic ->
          val month = selected as? String
          props.onFilterChange(
            filter.copy(month = if (month == null || month == "all") null else month)
          )
        }
      }
    }

    // ── Kategorie ─────────────────────────────────────────────────────────────
    div {
      div {
        className = ClassName("filter-label")
        +"Kategorie"
      }
      div {
        style = jso {
          display = Display.grid
          gap = 2.px
        }

        button {
          asDynamic().type = "button"
          className = ClassName("cat-item" + if (filter.category == "all") " is-active" else "")
          onClick = { props.onFilterChange(filter.copy(category = "all")) }

          span {
            className = ClassName("cat-dot")
            style = jso {
              background = Color(T.textMuted)
              opacity = 0.5.unsafeCast<web.cssom.Opacity>()
            }
          }
          span {
            className = ClassName("cat-item__name")
            +"Alle Kategorien"
          }
          span {
            className = ClassName("cat-item__count")
            +"${counts.values.sum()}"
          }
        }

        props.categories.forEach { category ->
          val isActive = filter.category == category.name
          val count = counts[category.name] ?: 0

          button {
            key = category.name
            asDynamic().type = "button"
            className = ClassName("cat-item" + if (isActive) " is-active" else "")
            style = jso {
              opacity = (if (count == 0 && !isActive) 0.45 else 1.0)
                .unsafeCast<web.cssom.Opacity>()
            }
            onClick = {
              props.onFilterChange(
                filter.copy(category = if (isActive) "all" else category.name)
              )
            }

            span {
              className = ClassName("cat-dot")
              style = jso { background = Color(category.color) }
            }
            span {
              className = ClassName("cat-item__name")
              +"${category.icon} ${category.label}"
            }
            span {
              className = ClassName("cat-item__count")
              +"$count"
            }
          }
        }
      }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    if (dirty) {
      Button {
        type = ButtonTypeText
        size = SizeSmall
        style = jso {
          color = Color(T.textSecondary)
          justifySelf = JustifySelf.start
        }
        onClick = {
          props.onFilterChange(TransactionFilter(category = "all", month = null, search = ""))
        }
        children = Fragment.create { +"Filter zurücksetzen" }
      }
    }
  }
}
