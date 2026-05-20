package components

import antd.Badge
import antd.Button
import antd.ButtonTypeDefault
import antd.Input
import antd.Select
import web.cssom.*
import com.banking.shared.data.Category
import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import utils.jso
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import web.html.HTMLInputElement

// ─── SidebarFilters ───────────────────────────────────────────────────────────

external interface SidebarFiltersProps : Props {
  var transactions: List<Transaction>
  var filter: TransactionFilter
  var onFilterChange: (TransactionFilter) -> Unit
  var onCollapse: () -> Unit
}

@JsName("SidebarFilters")
val SidebarFilters: FC<SidebarFiltersProps> = FC { props ->
  val categories = CategoryMapper.shared.getAllCategories()

  div {
    style = jso {
      width = 200.px
      height = 100.vh
      backgroundColor = Color("#0a0c10")
      borderRight = Border(0.5.px, LineStyle.solid, Color("#1e2330"))
      padding = 16.px
      display = Display.flex
      flexDirection = FlexDirection.column
      flexShrink = number(0.0)
    }

    // ── Logo ──────────────────────────────────────────────────────────────
    div {
      style = jso {
        display = Display.flex
        alignItems = AlignItems.center
        gap = 8.px
        marginBottom = 4.px
      }

      span {
        style = jso { color = Color("#60a5fa"); fontSize = 18.px }
        +"📊"
      }
      div {
        style = jso { fontSize = 15.px; fontWeight = FontWeight.bold; color = Color("#e2e8f0") }
        span { style = jso { color = Color("#60a5fa") }; +"Konto" }
        +"Analyse"
      }

      Button {
        type = ButtonTypeDefault
        size = "small"
        style = jso {
          marginLeft = "auto".unsafeCast<MarginLeft>()
          background = Color("#111127")
          color = Color("#FFFFFF")
        }
        onClick = { props.onCollapse() }
        children = Fragment.create { +"<" }
      }
    }

    // ── Sub-title ─────────────────────────────────────────────────────────
    div {
      style = jso {
        fontSize = 10.px
        color = Color("#475569")
        marginBottom = 20.px
        paddingLeft = 28.px
      }
      +"Banking Kategorien"
    }

    // ── Filter heading ────────────────────────────────────────────────────
    div {
      style = jso {
        fontSize = 10.px
        color = Color("#475569")
        letterSpacing = 0.08.em
        textTransform = TextTransform.uppercase
        marginBottom = 8.px
      }
      +"Filter"
    }

    // ── Search ────────────────────────────────────────────────────────────
    Input {
      placeholder = "Beschreibung..."
      value = props.filter.search
      prefix = Fragment.create {
        span {
          style = jso { color = Color("#64748b"); marginRight = 4.px; fontSize = 13.px }
          +"🔍"
        }
      }
      onChange = { e ->
        val target = e.target.unsafeCast<HTMLInputElement>()
        props.onFilterChange(props.filter.copy(search = target.value))
      }
      style = jso {
        marginBottom = 14.px
        borderRadius = 6.px
        background = Color("#1a1f2e")
        border = "0.5px solid #2d3548".unsafeCast<Border>()
        color = Color("#94a3b8")
        fontSize = 12.px
      }
    }

    // ── Month ─────────────────────────────────────────────────────────────
    div {
      style = jso { marginBottom = 12.px }

      div {
        style = jso {
          fontSize = 10.px
          color = Color("#475569")
          letterSpacing = 0.08.em
          textTransform = TextTransform.uppercase
          marginBottom = 6.px
        }
        +"Monat"
      }

      MonthSelector {
        transactions = props.transactions
        selectedMonth = props.filter.month
        onMonthSelected = { month ->
          props.onFilterChange(props.filter.copy(month = month))
        }
      }
    }

    // ── Category ──────────────────────────────────────────────────────────
    div {
      div {
        style = jso {
          fontSize = 10.px
          color = Color("#475569")
          letterSpacing = 0.08.em
          textTransform = TextTransform.uppercase
          marginBottom = 6.px
        }
        +"Kategorie"
      }

      CategoryList {
        transactions = props.transactions
        this.categories = categories
        selectedCategory = props.filter.category
        onCategorySelected = { name ->
          props.onFilterChange(
            props.filter.copy(
              category = if (props.filter.category == name) "all" else name
            )
          )
        }
      }
    }
  }
}

// ─── MonthSelector ────────────────────────────────────────────────────────────

external interface MonthSelectorProps : Props {
  var transactions: List<Transaction>
  var selectedMonth: String?
  var onMonthSelected: (String?) -> Unit
}

@JsName("MonthSelector")
val MonthSelector: FC<MonthSelectorProps> = FC { props ->
  val months = props.transactions
    .mapNotNull { it.date?.take(7) }
    .distinct()
    .sortedDescending()

  val dropdownOptions = buildList {
    add(jso<dynamic> { value = "all"; label = "Alle Monate" })
    months.forEach { month ->
      add(jso<dynamic> { value = month; label = month })
    }
  }.toTypedArray()

  Select {
    style = jso { width = 100.pct }
    value = props.selectedMonth ?: "all"
    onChange = { value: String? ->
      props.onMonthSelected(if (value.isNullOrEmpty() || value == "all") null else value)
    }
    this.options = dropdownOptions
  }
}

// ─── CategoryList ─────────────────────────────────────────────────────────────

external interface CategoryListProps : Props {
  var transactions: List<Transaction>
  var categories: List<Category>
  var selectedCategory: String
  var onCategorySelected: (String) -> Unit
}

@JsName("CategoryList")
val CategoryList: FC<CategoryListProps> = FC { props ->
  val categoryCounts = props.transactions
    .groupingBy { it.category }
    .eachCount()

  div {
    props.categories.forEach { category ->
      val isSelected = props.selectedCategory == category.name
      val count = categoryCounts[category.name] ?: 0
      // Selected tint: color at ~20% opacity (hex "33" suffix)
      val tintBg = if (isSelected) "${category.color}33" else "transparent"

      div {
        style = jso {
          display = Display.flex
          justifyContent = JustifyContent.spaceBetween
          alignItems = AlignItems.center
          padding = "5px 6px".unsafeCast<Padding>()
          marginBottom = 4.px
          borderRadius = 6.px
          cursor = Cursor.pointer
          background = Color(tintBg)
          border = (if (isSelected) "1px solid ${category.color}" else "1px solid transparent")
            .unsafeCast<Border>()
          transition = "all 0.2s".unsafeCast<Transition>()
        }
        onClick = { props.onCategorySelected(category.name) }

        // ── Dot + Label ────────────────────────────────────────────
        div {
          style = jso {
            display = Display.flex
            alignItems = AlignItems.center
            gap = 8.px
          }

          // Colored dot
          div {
            style = jso {
              width = 8.px
              height = 8.px
              borderRadius = 50.pct
              backgroundColor = Color(category.color)
              flexShrink = number(0.0)
            }
          }

          span {
            style = jso {
              fontSize = 12.px
              color = Color(if (isSelected) category.color else "#94a3b8")
            }
            +"${category.icon} ${category.label}"
          }
        }

        // ── Count badge ────────────────────────────────────────────
        Badge {
          this.count = count
          style = jso {
            backgroundColor = Color(category.color)
          }
        }
      }
    }
  }
}