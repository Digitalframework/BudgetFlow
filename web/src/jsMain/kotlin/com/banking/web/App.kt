package com.banking.web

import antd.*
import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.Transaction
import com.banking.shared.data.TransactionFilter
import web.cssom.*
import utils.jso
import web.file.File
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.useState
import org.w3c.files.FileReader
import org.w3c.files.File as W3CFile

import store.webStore
import web.html.HTMLElement
import components.*
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import react.useEffect
import react.useEffectOnce
import react.useRef
import utils.BankStatementParser

@JsModule("./pdfTextExtractor")
@JsNonModule
external object PdfTextExtractor {
  fun extractPdfLines(file: web.file.File): kotlin.js.Promise<Array<String>>
}


// Main entry point
fun main() {
  val root = document.getElementById("root") as? HTMLElement
  if (root != null) {
    createRoot(root).render(Fragment.create { BankingApp {} })
  }
}

// Main Banking App Component
@JsName("BankingApp")
val BankingApp: FC<Props> = FC {
  val store = webStore

  var transactions by useState<List<Transaction>>(emptyList())
  var loading by useState(false)
  var filter by useState(TransactionFilter())
  var pdfFileName by useState<String?>(null)
  var sidebarCollapsed by useState(false)
  var view by useState("table")
  var showClearDialog by useState(false)

  val categories = CategoryMapper.shared.getAllCategories()

  val unsubscribeRef = useRef<() -> Unit>(null)

  // Single unified hook handling initial load, registration, and cleanup
  useEffect(Unit) {
    store.loadPersistedData()
    transactions = store.getTransactions()
    filter = store.getFilter()
    pdfFileName = store.getPdfFileName()

    val unsubscribe = store.subscribe {
      transactions = store.getTransactions()
      loading = store.isLoading()
      filter = store.getFilter()
      pdfFileName = store.getPdfFileName()
    }

    try {
      awaitCancellation()
    } finally {
      unsubscribe()
    }
  }


  val filteredTransactions = transactions
    .filter { filter.category == "all" || it.category == filter.category }
    .filter { filter.month == null || it.date.startsWith(filter.month!!) }
    .filter { filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true) }

  val handleUpload: (File) -> Unit = { file ->
    store.setLoading(true)
    loading = true
    processFile(file) { parsedTransactions ->
      if (parsedTransactions.isNotEmpty()) {
        val withCategories = parsedTransactions.map { tx ->
          tx.copy(category = CategoryMapper.shared.categorizeTransaction(tx.description))
        }
        store.addTransactions(withCategories)
        store.setPdfFileName(file.name)
        pdfFileName = file.name
      }
      store.setLoading(false)
      loading = false
    }
  }
  div {
    style = jso {
      minHeight = 100.vh
      background = Color("#0d0d1a")
    }
    Layout {
      style = jso {
        height = 100.vh
        overflow = Overflow.hidden
        background = Color("#0d0d1a")
      }
      children = Fragment.create {
        if (!sidebarCollapsed) {
          Layout.Sider {
            theme = SiderThemeDark
            width = 220
            collapsed = false
            style = jso { background = Color("#111127") }

            children = Fragment.create {
              SidebarFilters {
                this.transactions = transactions
                this.filter = filter
                this.onFilterChange = { store.setFilter(it); filter = it }
                this.onCollapse = { sidebarCollapsed = true }
              }
            }

          }
        } else {
          Layout.Sider {
            theme = SiderThemeDark
            width = 50
            collapsed = true
            collapsible = true
            collapsedWidth = 50
            onCollapse = { collapsed, _ -> sidebarCollapsed = collapsed }
            style = jso {
              background = Color("#111127")
              cursor = "pointer".unsafeCast<Cursor>()
            }
            onClick = { sidebarCollapsed = false }
          }
        }

        div {
          style = jso {
            background = Color("#0d0d1a")
            width = 100.pct
            height = 100.vh
            overflow = Overflow.hidden
            display = Display.flex
            flexDirection = FlexDirection.column

          }

          Layout.Header {
            style = jso {
              background = Color("#111127")
              padding = "0 24px".unsafeCast<Padding>()
              display = Display.flex
              alignItems = AlignItems.center
              justifyContent = JustifyContent.spaceBetween
            }
            children = Fragment.create {
              HeaderContent {
                this.filteredCount = filteredTransactions.size
                this.pdfFileName = pdfFileName
                this.view = view
                this.onViewChange = { view = it }
                this.onClearClick = { showClearDialog = true }
                this.canClear = transactions.isNotEmpty()
              }
            }
          }
          Layout.Content {
            style = jso {
              flex = number(1.0)
              overflowY = "auto".unsafeCast<Overflow>()
              overflowX = Overflow.hidden
              padding = 24.px
              background = Color("#0d0d1a")
            }
            children = Fragment.create {
              if (loading) {
                Spin {
                  size = SpinSizeLarge
                }
              }

              UploadPanel { onUpload = handleUpload }

              if (transactions.isEmpty()) {
                EmptyState {}
              } else {
                SummaryCards {
                  this.transactions = filteredTransactions
                  this.categories = categories
                }

                if (view == "charts") {
                  CategoryBreakdown {
                    this.transactions = filteredTransactions
                    this.categories = categories
                  }
                } else {
                  TransactionTable {
                    this.transactions = filteredTransactions
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
  if (showClearDialog) {
    Modal {
      title = "Alle Daten löschen?"
      open = true
      onOk = {
        store.clearAll()
        transactions = emptyList()
        pdfFileName = null
        showClearDialog = false
      }
      onCancel = { showClearDialog = false }
      okText = "Löschen"
      cancelText = "Abbrechen"
      okButtonProps = jso { danger = true }
      children = Fragment.create {
        +"Alle importierten Transaktionen werden unwiderruflich entfernt."
      }
    }
  }
}


// Header Content Component
external interface HeaderContentProps : Props {
  var filteredCount: Int
  var pdfFileName: String?
  var view: String
  var onViewChange: (String) -> Unit
  var onClearClick: () -> Unit
  var canClear: Boolean
}

@JsName("HeaderContent")
val HeaderContent: FC<HeaderContentProps> = FC { props ->
  div {
    style = jso {
      display = Display.flex
      justifyContent = JustifyContent.spaceBetween
      alignItems = AlignItems.center
      width = 100.pct
    }

    // ── Left Side: Text and Filename ──────────────────────────────────────────
    div {
      style = jso {
        display = Display.flex
        alignItems = AlignItems.center
      }

      span {
        style = jso {
          fontSize = 16.px
          fontWeight = 600.unsafeCast<FontWeight>()
          color = Color("#ffffff")
        }
        +"${props.filteredCount} Transaktionen"

        if (props.pdfFileName != null) {
          span {
            style = jso {
              color = Color("#555555")
              fontSize = 12.px
              marginLeft = 8.px
            }
            +"(${props.pdfFileName})"
          }
        }
      }
    }

    // ── Right Side: Buttons ───────────────────────────────────────────────────
    div {
      style = jso {
        display = Display.flex
        gap = 16.px
        alignItems = AlignItems.center

      }

      // Simulating Button.Group manually
      div {
        style = jso { display = Display.flex
          }
        Button {
          type = if (props.view == "table") ButtonTypePrimary else ButtonTypeDefault
          onClick = { props.onViewChange("table") }
          // Assumes TableOutlined is imported from your ant-design/icons wrapper
          icon = TableOutlined.create()
          style = jso {
            borderTopRightRadius = 0.px
            borderBottomRightRadius = 0.px
          }
          children = Fragment.create { +"Tabelle" }
        }

        Button {
          type = if (props.view == "charts") ButtonTypePrimary else ButtonTypeDefault
          onClick = { props.onViewChange("charts") }
          // Assumes BarChartOutlined is imported from your ant-design/icons wrapper
          icon = BarChartOutlined.create()
          style = jso {
            borderTopLeftRadius = 0.px
            borderBottomLeftRadius = 0.px
            background = Color("#111127")
            color = Color("#ffffff")
          }
          children = Fragment.create { +"Charts" }
        }
      }

      Button {
        danger = true
        disabled = !props.canClear
        onClick = { props.onClearClick() }
        // Assumes DeleteOutlined is imported from your ant-design/icons wrapper
        icon = DeleteOutlined.create()
        style = jso {
          borderRadius = 8.px
          background = Color("#111127")
          if(!props.canClear) color = Color("#ffffff") else Color("#ff7875")

        }
        children = Fragment.create { +"Löschen" }
      }
    }
  }
}

// Empty State Component
@JsName("EmptyState")
val EmptyState: FC<Props> = FC {
  div {
    style = jso {
      display = Display.flex
      justifyContent = JustifyContent.center
      alignItems = AlignItems.center
      flexDirection = FlexDirection.column
      padding = 48.px
      textAlign = "center".unsafeCast<TextAlign>()
    }

    Empty {
      description = "Lade einen Kontoauszug hoch, um zu beginnen."
    }

    p {
      style = jso {
        color = Color("#555555")
        marginTop = 8.px
      }
      +"Die Daten werden lokal gespeichert."
    }
  }
}

private fun processFile(file: File, callback: (List<Transaction>) -> Unit) {
  GlobalScope.launch {
    try {
      val lines = PdfTextExtractor.extractPdfLines(file).await()
      val parsedTransactions = BankStatementParser.parseTransactions(lines.toList())
      println("transactions $parsedTransactions")
      callback(parsedTransactions)
    } catch (e: Throwable) {
      console.error("PDF extraction failed:", e)
      callback(emptyList())
    }
  }
}