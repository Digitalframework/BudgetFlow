package com.banking.web

import antd.*
import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import components.*
import design.T
import design.antdTheme
import design.fmtMonth
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useEffect
import react.useState
import store.webStore
import utils.BankStatementParser
import utils.jsObject
import utils.jso
import web.cssom.*
import web.file.File
import web.html.HTMLElement

/** Sider width; antd derives flex-basis / min / max / width from this. */
private const val SIDER_WIDTH = 318

@JsModule("./pdfTextExtractor")
@JsNonModule
external object PdfTextExtractor {
  fun extractPdfLines(file: File): kotlin.js.Promise<Array<String>>
}

// Main entry point
fun main() {
  val root = document.getElementById("root") as? HTMLElement
  if (root != null) {
    createRoot(root).render(
      Fragment.create {
        ConfigProvider {
          theme = antdTheme
          children = Fragment.create { BankingApp {} }
        }
      }
    )
  }
}

// Main Banking App Component
@JsName("BankingApp")
val BankingApp: FC<Props> = FC {
  val store = webStore

  var transactions by useState<List<Transaction>>(emptyList())
  var loading by useState(false)
  var filter by useState(TransactionFilter())
  var view by useState("overview")
  var collapsed by useState(false)
  var showClearDialog by useState(false)

  val categories = CategoryMapper.shared.getAllCategories()

  // Single unified hook handling initial load, registration, and cleanup
  useEffect(Unit) {
    store.loadPersistedData()
    transactions = store.getTransactions()
    filter = store.getFilter()

    val unsubscribe = store.subscribe {
      transactions = store.getTransactions()
      loading = store.isLoading()
      filter = store.getFilter()
    }

    try {
      awaitCancellation()
    } finally {
      unsubscribe()
    }
  }

  // Month + search apply everywhere; the category filter only narrows the
  // transaction list, so the category chart keeps showing the full split.
  val scoped = transactions
    .filter { filter.month == null || it.date.startsWith(filter.month!!) }
    .filter { filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true) }

  val filteredTx = scoped.filter { filter.category == "all" || it.category == filter.category }

  // The trend chart is the month picker, so it must ignore the month filter
  // while still respecting search + category.
  val allMonths = transactions
    .filter { filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true) }
    .filter { filter.category == "all" || it.category == filter.category }

  val hasData = transactions.isNotEmpty()

  val subtitle = listOfNotNull(
    if (filter.month != null) fmtMonth(filter.month) else "Alle Monate",
    if (filter.category != "all") {
      categories.find { it.name == filter.category }?.label ?: filter.category
    } else null,
    if (filter.search.isNotEmpty()) "„${filter.search}\"" else null,
  ).joinToString(" · ")

  val handleUpload: (File) -> Unit = { file ->
    store.setLoading(true)
    loading = true
    processFile(file) { parsed ->
      if (parsed.isEmpty()) {
        message.warning("Keine Transaktionen erkannt. Bitte prüfe das PDF-Format.")
      } else {
        val withCategories = parsed.map { tx ->
          tx.copy(category = CategoryMapper.shared.categorizeTransaction(tx.description))
        }
        store.addTransactions(withCategories)
        store.setPdfFileName(file.name)
        message.success("${parsed.size} Buchungen aus „${file.name}\" importiert")
      }
      store.setLoading(false)
      loading = false
    }
  }

  Layout {
    style = jso { minHeight = 100.vh }
    children = Fragment.create {

      // ── Sider ───────────────────────────────────────────────────────────────
      Layout.Sider {
        className = "app-sider"
        collapsible = true
        this.collapsed = collapsed
        onCollapse = { isCollapsed, _ -> collapsed = isCollapsed }
        width = SIDER_WIDTH
        collapsedWidth = 0
        trigger = null
        breakpoint = "lg"
        style = jso { background = Color(T.bg) }
        children = Fragment.create {
          div {
            style = jso {
              height = 60.px
              display = Display.flex
              alignItems = AlignItems.center
              padding = "0 20px".unsafeCast<Padding>()
              borderBottom = "1px solid ${T.border}".unsafeCast<BorderBottom>()
            }
            span {
              style = jso {
                fontSize = 16.px
                fontWeight = 700.unsafeCast<FontWeight>()
                color = Color(T.text)
              }
              +"Konto"
              span {
                style = jso { color = Color(T.accent) }
                +"Analyse"
              }
            }

            Button {
              type = ButtonTypeText
              icon = MenuFoldOutlined.create()
              onClick = { collapsed = true }
              style = jso {
                color = Color(T.textMuted)
                marginLeft = "auto".unsafeCast<MarginLeft>()
                marginRight = (-8).px
              }
            }
          }

          SidebarFilters {
            this.transactions = transactions
            this.categories = categories
            this.filter = filter
            this.onFilterChange = { newFilter ->
              store.setFilter(newFilter)
              filter = newFilter
            }
          }
        }
      }

      // ── Main column ─────────────────────────────────────────────────────────
      Layout {
        style = jso { background = Color(T.bg) }
        children = Fragment.create {

          Layout.Header {
            className = "app-header"
            style = jso {
              background = Color(T.bg)
              display = Display.flex
              alignItems = AlignItems.center
              justifyContent = JustifyContent.spaceBetween
              gap = 16.px
              padding = "0 24px".unsafeCast<Padding>()
            }
            children = Fragment.create {
              div {
                style = jso {
                  display = Display.flex
                  alignItems = AlignItems.center
                  gap = 12.px
                  minWidth = 0.px
                }

                // The fold button lives in the sider; once collapsed the sider is
                // 0 wide, so the way back has to sit here.
                if (collapsed) {
                  Button {
                    type = ButtonTypeText
                    icon = MenuUnfoldOutlined.create()
                    onClick = { collapsed = false }
                    style = jso {
                      color = Color(T.textMuted)
                      marginLeft = (-8).px
                    }
                  }
                }

                div {
                  style = jso { minWidth = 0.px }
                  div {
                    style = jso {
                      fontSize = 15.px
                      fontWeight = 600.unsafeCast<FontWeight>()
                      color = Color(T.text)
                    }
                    +(if (view == "overview") "Übersicht" else "Buchungen")
                  }
                  div {
                    style = jso {
                      fontSize = 12.px
                      color = Color(T.textMuted)
                      whiteSpace = WhiteSpace.nowrap
                      overflow = Overflow.hidden
                      textOverflow = TextOverflow.ellipsis
                    }
                    +(if (hasData) "${filteredTx.size} Buchungen · $subtitle" else "Noch keine Daten")
                  }
                }
              }

              Space {
                size = 10
                children = Fragment.create {
                  if (hasData) {
                    Segmented {
                      value = view
                      onChange = { selected: dynamic ->
                        (selected as? String)?.let { view = it }
                      }
                      options = arrayOf(
                        jsObject<dynamic> { option ->
                          option.value = "overview"
                          option.label = "Übersicht"
                          option.icon = PieChartOutlined.create()
                        },
                        jsObject<dynamic> { option ->
                          option.value = "table"
                          option.label = "Buchungen"
                          option.icon = UnorderedListOutlined.create()
                        },
                      )
                    }
                  }

                  UploadPanel {
                    onUpload = handleUpload
                    compact = true
                  }

                  if (hasData) {
                    Tooltip {
                      title = "Alle Daten löschen"
                      children = Fragment.create {
                        Button {
                          type = ButtonTypeText
                          danger = true
                          icon = DeleteOutlined.create()
                          onClick = { showClearDialog = true }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          Layout.Content {
            style = jso {
              padding = 24.px
              overflowY = "auto".unsafeCast<Overflow>()
            }
            children = Fragment.create {
              Spin {
                spinning = loading
                tip = "PDF wird analysiert…"
                size = SpinSizeLarge
                children = Fragment.create {
                  if (!hasData) {
                    UploadPanel { onUpload = handleUpload }
                  } else if (view == "overview") {
                    div {
                      style = jso {
                        display = Display.grid
                        gap = 14.px
                      }

                      StatTiles {
                        this.transactions = filteredTx
                        this.categories = categories
                      }

                      CategoryBreakdown {
                        this.transactions = scoped
                        this.categories = categories
                        this.activeCategory = filter.category
                        this.onSelectCategory = { category ->
                          val next = filter.copy(category = category)
                          store.setFilter(next)
                          filter = next
                        }
                      }

                      Row {
                        gutter = arrayOf(14, 14)
                        children = Fragment.create {
                          Col {
                            xs = 24; xl = 14
                            children = Fragment.create {
                              MonthlyTrend {
                                this.transactions = allMonths
                                this.activeMonth = filter.month
                                this.onSelectMonth = { month ->
                                  val next = filter.copy(month = month)
                                  store.setFilter(next)
                                  filter = next
                                }
                              }
                            }
                          }
                          Col {
                            xs = 24; xl = 10
                            children = Fragment.create {
                              TopMerchants {
                                this.transactions = filteredTx
                                this.categories = categories
                                this.onSelectMerchant = { name ->
                                  val next = filter.copy(search = name)
                                  store.setFilter(next)
                                  filter = next
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  } else {
                    TransactionTable {
                      this.transactions = filteredTx
                      this.categories = categories
                      this.onCategoryChange = { id, category ->
                        store.updateCategory(id, category)
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
  }

  if (showClearDialog) {
    Modal {
      title = "Alle Daten löschen?"
      open = true
      okText = "Löschen"
      cancelText = "Abbrechen"
      okButtonProps = jso { danger = true }
      onOk = {
        store.clearAll()
        transactions = emptyList()
        showClearDialog = false
        message.success("Daten gelöscht")
      }
      onCancel = { showClearDialog = false }
      children = Fragment.create {
        +"Alle importierten Transaktionen werden unwiderruflich entfernt."
      }
    }
  }
}

private fun processFile(file: File, callback: (List<Transaction>) -> Unit) {
  GlobalScope.launch {
    try {
      val lines = PdfTextExtractor.extractPdfLines(file).await()
      callback(BankStatementParser.parseTransactions(lines.toList()))
    } catch (e: Throwable) {
      console.error("PDF extraction failed:", e)
      message.error("Fehler beim Lesen der PDF")
      callback(emptyList())
    }
  }
}
