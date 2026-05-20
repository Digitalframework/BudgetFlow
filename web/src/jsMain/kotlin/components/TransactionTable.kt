package components

import antd.Table
import antd.TableColumnProps
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
import react.useState
import react.dom.events.MouseEvent

external interface TableRowProps : Props {
    var onClick: ((MouseEvent<*, *>) -> Unit)?
}

external interface TransactionTableProps : Props {
    var transactions: List<Transaction>
    var categories: List<Category>
    var onCategoryChange: (String, String) -> Unit
}

@JsName("TransactionTable")
val TransactionTable: FC<TransactionTableProps> = FC { props ->
    val (editingId, setEditingId) = useState<String?>(null)

    // Use dynamic array for columns
    val columns: dynamic = arrayOf(
        jso<TableColumnProps> {
            title = "DATUM"
            dataIndex = "date"
            key = "date"
            width = 100
            render = { value: dynamic, _: dynamic, _: dynamic ->
                Fragment.create {
                    span {
                        style = jso {
                            color = Color("#DDDDDD")
                            fontSize = 14.px
                        }
                        +value.toString()
                    }
                }.asDynamic()
            }
        },
        jso<TableColumnProps> {
            title = "BESCHREIBUNG"
            dataIndex = "description"
            key = "description"
            ellipsis = true
            render = { value: dynamic, _: dynamic, _: dynamic ->
                val displayValue = if (value != null) {
                    val s = value.toString()
                    if (s.length > 60) s.take(60) + "…" else s
                } else ""
                Fragment.create {
                    span {
                        style = jso {
                            color = Color("#DDDDDD")
                            fontSize = 14.px
                        }
                        +displayValue
                    }
                }.asDynamic()
            }
        },
        jso<TableColumnProps> {
            title = "BETRAG"
            dataIndex = "amount"
            key = "amount"
            width = 100
            align = "right"
            render = { value: dynamic, _: dynamic, _: dynamic ->
                val amount = (value as? Number)?.toDouble() ?: 0.0
                val formatted = "${if (amount >= 0) "+" else ""}€${(amount * 100).toLong() / 100.0}"
                val baseColor = if (amount >= 0) "#52C41A" else "#FF4D4F"
                Fragment.create {
                    span {
                        style = jso {
                            color = Color(baseColor)
                            fontWeight = FontWeight.bold
                            fontSize = 14.px
                        }
                        +formatted
                    }
                }.asDynamic()
            }
        },
        jso<TableColumnProps> {
            title = "KATEGORIE"
            dataIndex = "category"
            key = "category"
            width = 180
            jso<TableColumnProps> {
                title = "KATEGORIE"
                dataIndex = "category"
                key = "category"
                width = 180
                render = { value: dynamic, record: dynamic, _: dynamic ->
                    // ✅ Read directly from dynamic record, NOT cast to Transaction
                    val txId = record.id as? String ?: ""
                    val txCategory = record.category as? String ?: ""

                    val category = props.categories.find { it.name == txCategory }
                    val catColor = category?.color ?: "#8c8c8c"
                    val catIcon = category?.icon ?: "❓"
                    val catLabel = category?.label ?: txCategory
                    Fragment.create {
                        if (editingId == txId) {
                            div {
                                id = "category-select-${txId}"
                                style = jso { width = 100.pct }
                            }
                        } else {
                            span {
                                style = jso {
                                    color = Color(catColor)
                                    fontSize = 14.px
                                }
                                +"$catIcon $catLabel"
                            }
                        }
                    }.asDynamic()
                }
            }
        }
    )

    val dataSource = props.transactions.map { tx ->
        jso<dynamic> {
            this.id = tx.id
            this.date = tx.date
            this.description = tx.description
            this.amount = tx.amount
            this.category = tx.category
        }
    }

    div {
        style = jso {
            width = 100.pct
        }
        Table {
            this.columns = columns
            this.dataSource = dataSource.toTypedArray()
            this.pagination = jso {
                pageSize = 20
            }
            this.scroll = jso {
                y = 700
            }
            this.rowKey = "id"
            this.onRow = { record: dynamic, _: dynamic ->
        jso<TableRowProps> {
                    onClick = { _ ->
                        val id = record.id as? String
                        if (id != null) setEditingId(id)
                    }
                }
            }
            this.style = jso {
                background = Color("#111127")
            }
        }
    }
}
