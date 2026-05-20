package components

import antd.Card
import web.cssom.*
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import utils.jso
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface SummaryCardsProps : Props {
    var transactions: List<Transaction>
    var categories: List<Category>
}

@JsName("SummaryCards")
val SummaryCards: FC<SummaryCardsProps> = FC { props ->
    val totalExpenses = props.transactions
        .filter { it.amount > 0 }
        .sumOf { it.amount }

    div {
        style = jso {
            width = 100.pct
        }

        // Top row with two cards
        div {
            style = jso {
                display = Display.flex
                gap = 16.px
                marginBottom = 16.px
            }

            // Total Expenses Card
            Card {
                style = jso {
                    flex = number(1.0)
                    background = Color("#111127")
                    borderColor = Color("#222222")
                }
                children = Fragment.create {
                    div {
                        style = jso {
                            fontSize = 14.px
                            color = Color("#888888")
                            marginBottom = 8.px
                        }
                        +"Gesamtausgaben"
                    }
                    div {
                        style = jso {
                            fontSize = 24.px
                            fontWeight = FontWeight.bold
                            color = Color("#FF4D4F")
                        }
                        +"€ ${totalExpenses.asDynamic().toFixed(2)}"
                    }
                }
            }

            // Transaction Count Card
            Card {
                style = jso {
                    flex = number(1.0)
                    background = Color("#111127")
                    borderColor = Color("#222222")
                }
                children = Fragment.create {
                    div {
                        style = jso {
                            fontSize = 14.px
                            color = Color("#888888")
                            marginBottom = 8.px
                        }
                        +"Anzahl Buchungen"
                    }
                    div {
                        style = jso {
                            fontSize = 24.px
                            fontWeight = FontWeight.bold
                            color = Color("#1677FF")
                        }
                        +"${props.transactions.size}"
                    }
                }
            }
        }

        // Category Breakdown Card
        CategoryBreakdownCard {
            transactions = props.transactions
            categories = props.categories
        }
    }
}

external interface CategoryBreakdownCardProps : Props {
    var transactions: List<Transaction>
    var categories: List<Category>
}

@JsName("CategoryBreakdownCard")
val CategoryBreakdownCard: FC<CategoryBreakdownCardProps> = FC { props ->
    Card {
        style = jso {
            width = 100.pct
            background = Color("#111127")
            borderColor = Color("#222222")
        }
        children = Fragment.create {
            div {
                style = jso {
                    display = Display.flex
                    alignItems = AlignItems.center
                    marginBottom = 16.px
                }
                span {
                    style = jso {
                        fontSize = 20.px
                        marginRight = 8.px
                    }
                    +"📊"
                }
                span {
                    style = jso {
                        fontSize = 16.px
                        fontWeight = FontWeight.bold
                        color = Color("#DDDDDD")
                    }
                    +"Ausgaben nach Kategorie"
                }
            }

            if (props.transactions.isEmpty()) {
                div {
                    style = jso {
                        color = Color("#888888")
                        fontSize = 14.px
                    }
                    +"Keine Daten"
                }
            } else {
                val byCategory = props.transactions
                    .filter { it.amount > 0 }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }

                val maxTotal = byCategory.firstOrNull()?.second ?: 1.0

                byCategory.forEach { (category, total) ->
                    val cat = props.categories.find { it.name == category }
                    val baseColor = cat?.color ?: "#8c8c8c"

                    div {
                        style = jso {
                            marginBottom = 8.px
                        }

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
                                    this.color = Color(baseColor)
                                }
                                +"€ ${total.asDynamic().toFixed(2)}"
                            }
                        }

                        // Progress bar
                        div {
                            style = jso {
                                width = 100.pct
                                height = 8.px
                                background = Color("#1a1a2e")
                                borderRadius = 4.px
                                overflow = Overflow.hidden
                            }
                            div {
                                style = jso {
                                    width = ((total / maxTotal) * 100).pct
                                    height = 100.pct
                                    background = Color(baseColor)
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