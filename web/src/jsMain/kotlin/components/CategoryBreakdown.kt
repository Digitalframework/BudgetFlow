package components

import antd.Card
import web.cssom.*
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import utils.jso
import react.FC
import react.Props
import react.Fragment
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import web.cssom.FontWeight
import web.cssom.Color
import web.cssom.Display

external interface CategoryBreakdownProps : Props {
    var transactions: List<Transaction>
    var categories: List<Category>
}

@JsName("CategoryBreakdown")
val CategoryBreakdown: FC<CategoryBreakdownProps> = FC { props ->
    Card {
        style = jso {
            width = "100%"
            background = "#111127"
            borderColor = "#222222"
        }

        // FIX: Assign elements directly to the 'children' prop
        // to establish a valid ChildrenBuilder context.
        children = Fragment.create {
            div {
                style = jso {
                    display = Display.flex
                    alignItems = AlignItems.center
                    marginBottom = 16.px
                }
                span {
                    style = jso { fontSize = 20.px; marginRight = 8.px }
                    +"📊"
                }
                span {
                    style = jso {
                        fontSize = 16.px
                        fontWeight = FontWeight.bold
                        color = Color("#DDDDDD")
                    }
                    +"Kategorien-Übersicht"
                }
            }

            if (props.transactions.isEmpty()) {
                div {
                    style = jso {
                        color = Color("#888888")
                        fontSize = 14.px
                        textAlign = "center".unsafeCast<TextAlign>()
                        padding = 24.px
                    }
                    +"Keine Daten"
                }
            } else {
                val byCategory = props.transactions
                    .filter { it.amount < 0 }
                    .groupBy { it.category }
                    .mapValues { kotlin.math.abs(it.value.sumOf { t -> t.amount }) }
                    .toList()
                    .sortedByDescending { it.second }

                val maxTotal = byCategory.firstOrNull()?.second ?: 1.0

                div {
                    byCategory.forEach { (category, total) ->
                        val cat = props.categories.find { it.name == category }
                        val baseColor = cat?.color ?: "#8c8c8c"

                        div {
                            style = jso { marginBottom = 8.px }

                            // Label row
                            div {
                                style = jso {
                                    display = Display.flex
                                    justifyContent = JustifyContent.spaceBetween
                                    alignItems = AlignItems.center
                                    marginBottom = 4.px
                                }
                                span {
                                    style = jso {
                                        fontSize = 14.px
                                        color = Color("#DDDDDD")
                                    }
                                    +"${cat?.icon ?: "❓"} ${cat?.label ?: category}"
                                }
                                span {
                                    style = jso {
                                        fontSize = 14.px
                                        fontWeight = FontWeight.bold
                                        color = Color(baseColor)
                                    }
                                    +"€ ${total.asDynamic().toFixed(2)}"
                                }
                            }

                            // Progress bar background
                            div {
                                style = jso {
                                    width = 100.pct
                                    height = 8.px
                                    backgroundColor = Color("#1a1a2e")
                                    borderRadius = 4.px
                                    overflow = Overflow.hidden
                                }
                                // Progress bar fill
                                div {
                                    style = jso {
                                        width = ((total / maxTotal) * 100).pct
                                        height = 100.pct
                                        backgroundColor = Color(baseColor)
                                        borderRadius = 4.px
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