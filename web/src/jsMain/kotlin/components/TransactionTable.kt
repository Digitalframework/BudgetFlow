package components

import antd.Select
import antd.Table
import antd.TableColumnProps
import antd.Tag
import antd.Tooltip
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

    val columns: dynamic = arrayOf(

        // ── Datum ─────────────────────────────────────────────────────────────
        jso<TableColumnProps> {
            title = "Datum"
            dataIndex = "date"
            key = "date"
            width = 110
            sorter = { a: dynamic, b: dynamic ->
                (a.date as? String ?: "").compareTo(b.date as? String ?: "")
            }
            render = { value: dynamic, _: dynamic, _: dynamic ->
                Fragment.create {
                    span {
                        style = jso {
                            fontFamily = string("'DM Mono', monospace")
                            fontSize = 12.px
                            color = Color("#64748b")
                        }
                        +(value?.toString() ?: "")
                    }
                }.asDynamic()
            }
        },

        // ── Beschreibung ──────────────────────────────────────────────────────
        jso<TableColumnProps> {
            title = "Beschreibung"
            dataIndex = "description"
            key = "description"
            ellipsis = true
            render = { value: dynamic, _: dynamic, _: dynamic ->
                val raw = value?.toString() ?: ""
                val display = if (raw.length > 60) raw.take(60) + "…" else raw
                Fragment.create {
                    Tooltip {
                        title = raw
                        children = Fragment.create {
                            span {
                                style = jso {
                                    fontSize = 13.px
                                    color = Color("#e2e8f0")
                                }
                                +display
                            }
                        }
                    }
                }.asDynamic()
            }
        },

        // ── Betrag ────────────────────────────────────────────────────────────
        jso<TableColumnProps> {
            title = "Betrag"
            dataIndex = "amount"
            key = "amount"
            width = 120
            align = "right"
            sorter = { a: dynamic, b: dynamic ->
                val aVal = (a.amount as? Number)?.toDouble() ?: 0.0
                val bVal = (b.amount as? Number)?.toDouble() ?: 0.0
                aVal.compareTo(bVal)
            }
            render = { value: dynamic, _: dynamic, _: dynamic ->
                val amount = (value as? Number)?.toDouble() ?: 0.0
                val formatted = "${if (amount >= 0) "+" else ""} ${amount.asDynamic().toFixed(2)}€"
                val baseColor = if (amount >= 0) "#52c41a" else "#ff4d4f"
                Fragment.create {
                    span {
                        style = jso {
                            color = Color(baseColor)
                            fontWeight = FontWeight.bold
                            fontFamily = string("'DM Mono', monospace")
                            fontSize = 12.px
                        }
                        +formatted
                    }
                }.asDynamic()
            }
        },

        // ── Kategorie ─────────────────────────────────────────────────────────
        jso<TableColumnProps> {
            title = "Kategorie"
            dataIndex = "category"
            key = "category"
            width = 200
            render = { _: dynamic, record: dynamic, _: dynamic ->
                val txId = record.id as? String ?: ""
                val txCategory = record.category as? String ?: ""
                val category = props.categories.find { it.name == txCategory }
                val catColor = category?.color ?: "#8c8c8c"
                val catIcon = category?.icon ?: "❓"
                val catLabel = category?.label ?: txCategory

                Fragment.create {
                    if (editingId == txId) {
                        // ── Inline category select ─────────────────────────
                        val options = props.categories.map { cat ->
                            jso<dynamic> {
                                value = cat.name
                                label = "${cat.icon} ${cat.label}"
                            }
                        }.toTypedArray()

                        Select {
                            defaultValue = txCategory
                            size = "small"
                            //autoFocus = true
                            style = jso { width = 180.px }
                            this.options = options
                            onChange = { value: String? ->
                                if (value != null) {
                                    props.onCategoryChange(txId, value)
                                }
                                setEditingId(null)
                            }
                            //nBlur = { setEditingId(null) }
                        }
                    } else {
                        // ── Category tag ───────────────────────────────────
                        Tag {
                            color = catColor
                            style = jso {
                                cursor = Cursor.pointer
                                borderRadius = 8.px
                                fontSize = 12.px
                            }
                            //onClick = { setEditingId(txId) }
                            children = Fragment.create {
                                +"$catIcon $catLabel"
                            }
                        }
                    }
                }.asDynamic()
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
        style = jso { width = 100.pct }

        Table {
            this.columns = columns
            this.dataSource = dataSource.toTypedArray()
            this.rowKey = "id"
            this.size = "small"
            this.pagination = jso {
                pageSize = 25
                showSizeChanger = true
                pageSizeOptions = arrayOf("10", "25", "50", "100")
            }
            this.scroll = jso { x = 700 }
            this.style = jso {
                borderRadius = 12.px
                overflow = Overflow.hidden
            }
            this.rowClassName = { record: dynamic, _: dynamic ->
                val amount = (record.amount as? Number)?.toDouble() ?: 0.0
                if (amount >= 0) "row-income" else "row-expense"
            }
            this.onRow = { record: dynamic, _: dynamic ->
                jso<TableRowProps> {
                    onClick = { _ ->
                        val id = record.id as? String
                        if (id != null) setEditingId(id)
                    }
                }
            }
        }
    }
}