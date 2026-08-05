package components

import antd.Select
import antd.Table
import antd.TableColumnProps
import antd.Tooltip
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import design.T
import design.fmtDay
import design.fmtEur
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState
import utils.jsObject
import utils.jso
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.FontFamily
import web.cssom.FontWeight
import web.cssom.Overflow
import web.cssom.WhiteSpace
import web.cssom.pct
import web.cssom.px

external interface TransactionTableProps : Props {
  var transactions: List<Transaction>
  var categories: List<Category>
  var onCategoryChange: (String, String) -> Unit
}

@JsName("TransactionTable")
val TransactionTable: FC<TransactionTableProps> = FC { props ->
  var editingId by useState<String?>(null)

  val categoryOptions = props.categories.map { category ->
    jsObject<dynamic> { option ->
      option.value = category.name
      option.label = "${category.icon} ${category.label}"
    }
  }.toTypedArray()

  val columns: dynamic = arrayOf(

    // ── Datum ─────────────────────────────────────────────────────────────────
    jso<TableColumnProps> {
      title = "Datum"
      dataIndex = "date"
      key = "date"
      width = 120
      defaultSortOrder = "descend"
      sorter = { a: dynamic, b: dynamic ->
        ((a.date as? String) ?: "").compareTo((b.date as? String) ?: "")
      }
      render = { value: dynamic, _: dynamic, _: dynamic ->
        val iso = value as? String ?: ""
        Fragment.create {
          span {
            style = jso {
              fontFamily = T.mono.unsafeCast<FontFamily>()
              fontSize = 12.px
              color = Color(T.textSecondary)
              whiteSpace = WhiteSpace.nowrap
            }
            +"${fmtDay(iso)} ${iso.take(4)}"
          }
        }.asDynamic()
      }
    },

    // ── Beschreibung ──────────────────────────────────────────────────────────
    jso<TableColumnProps> {
      title = "Beschreibung"
      dataIndex = "description"
      key = "description"
      ellipsis = true
      render = { value: dynamic, _: dynamic, _: dynamic ->
        val text = value as? String ?: ""
        Fragment.create {
          Tooltip {
            title = text
            mouseEnterDelay = 0.4
            children = span.create {
              style = jso {
                fontSize = 13.px
                color = Color(T.text)
              }
              +text
            }
          }
        }.asDynamic()
      }
    },

    // ── Kategorie ─────────────────────────────────────────────────────────────
    jso<TableColumnProps> {
      title = "Kategorie"
      dataIndex = "category"
      key = "category"
      width = 230
      filters = props.categories.map { category ->
        jsObject<dynamic> { filterItem ->
          filterItem.text = category.label
          filterItem.value = category.name
        }
      }.toTypedArray()
      onFilter = { value: dynamic, record: dynamic -> record.category == value }
      render = { value: dynamic, record: dynamic, _: dynamic ->
        val txId = record.id as? String ?: ""
        val txCategory = value as? String ?: ""
        val category = props.categories.find { it.name == txCategory }

        Fragment.create {
          if (editingId == txId) {
            Select {
              defaultValue = txCategory
              size = "small"
              style = jso { width = 200.px }
              defaultOpen = true
              autoFocus = true
              options = categoryOptions
              onChange = { selected: dynamic ->
                val name = selected as? String
                if (name != null) props.onCategoryChange(txId, name)
                editingId = null
              }
              onBlur = { editingId = null }
            }
          } else {
            span {
              className = ClassName("tx-cat")
              onClick = { editingId = txId }
              title = "Kategorie ändern"
              span {
                className = ClassName("cat-dot")
                style = jso {
                  background = Color(category?.color ?: T.textMuted)
                  width = 7.px
                  height = 7.px
                  borderRadius = 50.pct
                }
              }
              +(category?.label ?: txCategory)
            }
          }
        }.asDynamic()
      }
    },

    // ── Betrag ────────────────────────────────────────────────────────────────
    jso<TableColumnProps> {
      title = "Betrag"
      dataIndex = "amount"
      key = "amount"
      width = 130
      align = "right"
      sorter = { a: dynamic, b: dynamic ->
        val aVal = (a.amount as? Number)?.toDouble() ?: 0.0
        val bVal = (b.amount as? Number)?.toDouble() ?: 0.0
        aVal.compareTo(bVal)
      }
      render = { value: dynamic, _: dynamic, _: dynamic ->
        val amount = (value as? Number)?.toDouble() ?: 0.0
        Fragment.create {
          span {
            style = jso {
              color = Color(T.text)
              fontFamily = T.mono.unsafeCast<FontFamily>()
              fontSize = 13.px
              fontWeight = 500.unsafeCast<FontWeight>()
            }
            +fmtEur(amount)
          }
        }.asDynamic()
      }
    },
  )

  val dataSource = props.transactions.map { tx ->
    jso<dynamic> {
      id = tx.id
      date = tx.date
      description = tx.description
      amount = tx.amount
      category = tx.category
    }
  }.toTypedArray()

  val total = props.transactions.sumOf { it.amount }

  div {
    className = ClassName("panel")
    style = jso { overflow = Overflow.hidden }

    Table {
      this.columns = columns
      this.dataSource = dataSource
      rowKey = "id"
      size = "small"
      scroll = jsObject<dynamic> { it.x = 640 }
      pagination = jsObject<dynamic> { pager ->
        pager.pageSize = 25
        pager.showSizeChanger = true
        pager.pageSizeOptions = arrayOf("25", "50", "100")
        pager.size = "small"
        pager.style = jsObject<dynamic> { pagerStyle ->
          pagerStyle.padding = "0 18px"
          pagerStyle.marginBottom = "14px"
        }
        pager.showTotal = { count: dynamic, range: dynamic ->
          Fragment.create {
            span {
              style = jso {
                color = Color(T.textMuted)
                fontSize = 12.px
              }
              +"${range[0]}–${range[1]} von $count"
            }
          }.asDynamic()
        }
      }
      summary = { _: dynamic ->
        Fragment.create {
          Table.Summary {
            fixed = true
            children = Fragment.create {
              Table.Summary.Row {
                children = Fragment.create {
                  Table.Summary.Cell {
                    index = 0
                    colSpan = 3
                    children = Fragment.create {
                      span {
                        style = jso {
                          color = Color(T.textMuted)
                          fontSize = 12.px
                        }
                        +"Summe der Auswahl"
                      }
                    }
                  }
                  Table.Summary.Cell {
                    index = 3
                    align = "right"
                    children = Fragment.create {
                      span {
                        style = jso {
                          fontFamily = T.mono.unsafeCast<FontFamily>()
                          fontWeight = 600.unsafeCast<FontWeight>()
                        }
                        +fmtEur(total)
                      }
                    }
                  }
                }
              }
            }
          }
        }.asDynamic()
      }
    }
  }
}
