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
import antd.Tag

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
            width = 220.px
            height = 100.vh
            backgroundColor = Color("#111127")
            padding = 16.px
            display = Display.flex
            flexDirection = FlexDirection.column
        }

        // ── Header ────────────────────────────────────────────────────────────
        div {
            style = jso {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
                marginBottom = 8.px
            }

            div {
                style = jso {
                    fontSize = 16.px
                    fontWeight = FontWeight.bold
                    color = Color("#ffffff")
                }
                +"💰 KontoAnalyse"
            }

            Button {
                type = ButtonTypeDefault
                size = "small"
                onClick = { props.onCollapse() }
                children = Fragment.create { +"<" }
            }
        }

        // ── Subtitle ──────────────────────────────────────────────────────────
        div {
            style = jso {
                fontSize = 12.px
                color = Color("#555555")
                marginBottom = 16.px
            }
            +"Banking Kategorien"
        }

        // ── Search ────────────────────────────────────────────────────────────
        span {
            style = jso {
                fontSize = 12.px
                color = Color("#AAAAAA")
                display = Display.block
                marginBottom = 6.px
            }
            +"SUCHE"
        }

        Input {
            placeholder = "Beschreibung..."
            value = props.filter.search
            prefix = Fragment.create {
                span {
                    style = jso { color = Color("#AAAAAA"); marginRight = 4.px; fontSize = 12.px }
                    +"🔍"
                }
            }
            onChange = { e ->
                val target = e.target.unsafeCast<HTMLInputElement>()
                props.onFilterChange(props.filter.copy(search = target.value))
            }
            style = jso {
                marginBottom = 16.px
                borderRadius = 8.px
                background = Color("#1a1a2e")
                border = "1px solid #333333".unsafeCast<Border>()
                color = Color("#ffffff")
            }
        }

        // ── Month ─────────────────────────────────────────────────────────────
        span {
            style = jso {
                fontSize = 12.px
                color = Color("#AAAAAA")
                display = Display.block
                marginBottom = 6.px
            }
            +"MONAT"
        }

        MonthSelector {
            transactions = props.transactions
            selectedMonth = props.filter.month
            onMonthSelected = { month ->
                props.onFilterChange(props.filter.copy(month = month))
            }
        }

        // ── Category ──────────────────────────────────────────────────────────
        span {
            style = jso {
                marginTop = 16.px
                fontSize = 12.px
                color = Color("#AAAAAA")
                display = Display.block
                marginBottom = 6.px
            }
            +"KATEGORIE"
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
        style = jso {
            width = 100.pct
        }
        value = props.selectedMonth ?: "all"
        onChange = { value: String? ->
            props.onMonthSelected(if (value.isNullOrEmpty()) null else value)
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

            div {
                style = jso {
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                    alignItems = AlignItems.center
                    paddingTop = 6.px
                    paddingBottom = 6.px
                    paddingLeft = 10.px
                    paddingRight = 10.px
                    marginBottom = 4.px
                    borderRadius = 8.px
                    cursor = Cursor.pointer
                    background = if (isSelected) Color(category.color) else Color("transparent")
                    borderTop = if (isSelected) Border(1.px, LineStyle.solid, Color(category.color)) else Border(1.px, LineStyle.solid, Color("transparent"))
                    borderBottom = if (isSelected) Border(1.px, LineStyle.solid, Color(category.color)) else Border(1.px, LineStyle.solid, Color("transparent"))
                    borderLeft = if (isSelected) Border(1.px, LineStyle.solid, Color(category.color)) else Border(1.px, LineStyle.solid, Color("transparent"))
                    borderRight = if (isSelected) Border(1.px, LineStyle.solid, Color(category.color)) else Border(1.px, LineStyle.solid, Color("transparent"))
                }
                onClick = { props.onCategorySelected(category.name) }

                // ── Label ──────────────────────────────────────────────────
                div {
                    style = jso {
                        fontSize = 14.px
                        color = Color(if (isSelected) category.color else "#CCCCCC")
                    }
                    +"${category.icon} ${category.label}"
                }

                // ── Count badge ────────────────────────────────────────────
                Tag {
                    color = category.color
                    style = jso { fontSize = 12.px }
                    children = Fragment.create { +count.toString() }
                }
            }
        }
    }
}