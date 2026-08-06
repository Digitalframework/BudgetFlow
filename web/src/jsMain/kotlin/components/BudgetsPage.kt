package components

import antd.ArrowLeftOutlined
import antd.Button
import antd.ButtonTypeDefault
import antd.ButtonTypePrimary
import antd.ButtonTypeText
import antd.Empty
import antd.Input
import antd.RightOutlined
import antd.SizeLarge
import antd.SizeSmall
import antd.message
import com.banking.shared.data.Category
import com.banking.shared.data.Transaction
import design.T
import design.fmtDay
import design.fmtEur
import design.fmtEurShort
import design.fmtFixed
import design.fmtMonth
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState
import utils.jso
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.Margin
import web.cssom.pct
import web.cssom.px
import web.html.HTMLInputElement
import kotlin.math.abs
import kotlin.math.min

/**
 * Monthly spending limits per category. The list shows how much of each limit
 * the selected month has already used; picking a row opens the editor, which
 * returns via the arrow on the left. Limits live in localStorage (see WebStore),
 * so a category without a saved value has to be set up first.
 */
external interface BudgetsPageProps : Props {
  /** Unfiltered — the page scopes to [month] itself so the numbers stay comparable. */
  var transactions: List<Transaction>
  var categories: List<Category>
  var budgets: Map<String, Double>

  /** Month filter from the sidebar; null falls back to the newest month with data. */
  var month: String?
  var onSetBudget: (String, Double?) -> Unit
}

@JsName("BudgetsPage")
val BudgetsPage: FC<BudgetsPageProps> = FC { props ->
  var editing by useState<String?>(null)
  var draft by useState("")

  val month = props.month ?: props.transactions
    .map { it.date.take(7) }
    .filter { it.isNotEmpty() }
    .maxOrNull()

  val monthTx = props.transactions.filter { month != null && it.date.startsWith(month) }
  val spentByCategory = monthTx
    .groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { abs(it.amount) } }

  val rows = props.categories.map { category ->
    BudgetRow(
      category = category,
      limit = props.budgets[category.name],
      spent = spentByCategory[category.name] ?: 0.0,
      count = monthTx.count { it.category == category.name },
    )
  }

  val (budgeted, unbudgeted) = rows.partition { it.limit != null }

  val openEditor: (BudgetRow) -> Unit = { row ->
    editing = row.category.name
    draft = row.limit?.let { fmtFixed(it, 0) } ?: ""
  }

  // ── Editor ──────────────────────────────────────────────────────────────────
  val editingRow = rows.find { it.category.name == editing }
  if (editingRow != null) {
    val row = editingRow
    val parsed = draft.trim().replace(",", ".").toDoubleOrNull()
    val preview = parsed ?: row.limit

    div {
      style = jso {
        display = Display.grid
        gap = 14.px
      }

      // Back to the list.
      div {
        style = jso {
          display = Display.flex
          alignItems = AlignItems.center
          gap = 10.px
        }
        Button {
          type = ButtonTypeText
          icon = ArrowLeftOutlined.create()
          onClick = { editing = null }
          style = jso {
            color = Color(T.textSecondary)
            marginLeft = (-8).px
          }
          children = Fragment.create { +"Budgets" }
        }
      }

      Panel {
        title = "${row.category.icon} ${row.category.label}"
        extra = if (month != null) fmtMonth(month) else "Kein Zeitraum"
        children = Fragment.create {
          div {
            className = ClassName("budget-editor")

            div {
              className = ClassName("filter-label")
              +"Monatliches Limit"
            }
            Input {
              type = "number"
              size = SizeLarge
              placeholder = "z. B. 400"
              value = draft
              suffix = "€"
              onChange = { event ->
                draft = event.target.unsafeCast<HTMLInputElement>().value
              }
            }

            div {
              className = ClassName("budget-presets")
              listOf(50.0, 100.0, 200.0, 400.0, 800.0).forEach { preset ->
                Button {
                  key = "$preset"
                  size = SizeSmall
                  type = ButtonTypeDefault
                  onClick = { draft = fmtFixed(preset, 0) }
                  children = Fragment.create { +fmtEurShort(preset) }
                }
              }
            }

            // Live preview against the month that is actually shown.
            BudgetMeter {
              this.spent = row.spent
              this.limit = preview
            }

            div {
              className = ClassName("budget-editor__stats")
              val bookings = if (row.count == 1) "Buchung" else "Buchungen"
              val limitText = preview?.let { "Limit ${fmtEur(it)}" } ?: "kein Limit"
              +"${fmtEur(row.spent)} ausgegeben · ${row.count} $bookings · $limitText"
            }

            div {
              className = ClassName("budget-editor__actions")
              Button {
                type = ButtonTypePrimary
                disabled = parsed == null || parsed <= 0.0
                onClick = {
                  val value = draft.trim().replace(",", ".").toDoubleOrNull()
                  if (value != null && value > 0.0) {
                    props.onSetBudget(row.category.name, value)
                    message.success("Budget für ${row.category.label} gespeichert")
                    editing = null
                  }
                }
                children = Fragment.create { +"Speichern" }
              }
              if (row.limit != null) {
                Button {
                  type = ButtonTypeText
                  danger = true
                  onClick = {
                    props.onSetBudget(row.category.name, null)
                    message.success("Budget für ${row.category.label} entfernt")
                    editing = null
                  }
                  children = Fragment.create { +"Budget entfernen" }
                }
              }
            }
          }
        }
      }

      // The bookings behind the number, so the limit can be judged in context.
      val recent = monthTx
        .filter { it.category == row.category.name }
        .sortedByDescending { abs(it.amount) }
        .take(6)

      Panel {
        title = "Größte Buchungen"
        extra = if (month != null) fmtMonth(month) else null
        children = Fragment.create {
          if (recent.isEmpty()) {
            Empty {
              description = "Keine Buchungen in diesem Zeitraum"
              image = null
              style = jso { margin = "16px 0".unsafeCast<Margin>() }
            }
          } else {
            recent.forEach { tx ->
              div {
                key = tx.id
                className = ClassName("budget-tx")
                span {
                  className = ClassName("budget-tx__date")
                  +fmtDay(tx.date)
                }
                span {
                  className = ClassName("budget-tx__name")
                  +tx.description
                }
                span {
                  className = ClassName("budget-tx__value")
                  +fmtEur(abs(tx.amount))
                }
              }
            }
          }
        }
      }
    }
    return@FC
  }

  // ── List ────────────────────────────────────────────────────────────────────
  div {
    style = jso {
      display = Display.grid
      gap = 14.px
    }

    div {
      div {
        className = ClassName("budget-intro__title")
        +"Budgets"
      }
      div {
        className = ClassName("budget-intro__text")
        +("Setze dir selbst Ausgabenlimits pro Kategorie. Sie werden lokal " +
          "gespeichert und gegen die Ausgaben des gewählten Monats gerechnet.")
      }
    }

    // Total across every category that has a limit.
    if (budgeted.isNotEmpty()) {
      val totalLimit = budgeted.sumOf { it.limit ?: 0.0 }
      val totalSpent = budgeted.sumOf { it.spent }

      Panel {
        title = if (month != null) fmtMonth(month) else "Aktueller Monat"
        extra = "${budgeted.size} von ${props.categories.size} Kategorien"
        children = Fragment.create {
          BudgetMeter {
            this.spent = totalSpent
            this.limit = totalLimit
          }
        }
      }
    }

    Panel {
      title = "Budgets pro Kategorie"
      extra = if (budgeted.isEmpty()) "Noch kein Budget gesetzt" else null
      children = Fragment.create {
        budgeted
          .sortedByDescending { it.usage }
          .forEach { row ->
            BudgetCard {
              key = row.category.name
              this.icon = row.category.icon
              this.label = row.category.label
              this.color = row.category.color
              this.spent = row.spent
              this.limit = row.limit
              this.count = row.count
              this.onOpen = { openEditor(row) }
            }
          }

        if (unbudgeted.isNotEmpty()) {
          div {
            className = ClassName("filter-label")
            style = jso {
              marginTop = if (budgeted.isEmpty()) 0.px else 18.px
              marginBottom = 8.px
            }
            +"Ohne Budget"
          }
          unbudgeted
            .sortedByDescending { it.spent }
            .forEach { row ->
              BudgetCard {
                key = row.category.name
                this.icon = row.category.icon
                this.label = row.category.label
                this.color = row.category.color
                this.spent = row.spent
                this.limit = row.limit
                this.count = row.count
                this.onOpen = { openEditor(row) }
              }
            }
        }
      }
    }
  }
}

// ── Row ───────────────────────────────────────────────────────────────────────

private data class BudgetRow(
  val category: Category,
  val limit: Double?,
  val spent: Double,
  val count: Int,
) {
  /** 0..n share of the limit already spent; 0 while no limit is set. */
  val usage: Double get() = limit?.takeIf { it > 0.0 }?.let { spent / it } ?: 0.0
}

external interface BudgetCardProps : Props {
  var icon: String
  var label: String
  var color: String
  var spent: Double
  var limit: Double?
  var count: Int
  var onOpen: () -> Unit
}

/** One tappable category row: identity on the left, meter across the bottom. */
@JsName("BudgetCard")
val BudgetCard: FC<BudgetCardProps> = FC { props ->
  button {
    asDynamic().type = "button"
    className = ClassName("budget-card")
    onClick = { props.onOpen() }

    div {
      className = ClassName("budget-card__head")
      span {
        className = ClassName("budget-card__icon")
        style = jso { background = Color(props.color + "22") }
        +props.icon
      }
      span {
        className = ClassName("budget-card__name")
        +props.label
      }
      span {
        className = ClassName("budget-card__count")
        +"${props.count}"
      }
      span {
        className = ClassName("budget-card__chevron")
        +RightOutlined.create()
      }
    }

    if (props.limit == null) {
      div {
        className = ClassName("budget-card__unset")
        +"Kein Budget · ${fmtEurShort(props.spent)} ausgegeben — jetzt festlegen"
      }
    } else {
      BudgetMeter {
        this.spent = props.spent
        this.limit = props.limit
      }
    }
  }
}

// ── Meter ─────────────────────────────────────────────────────────────────────

external interface BudgetMeterProps : Props {
  var spent: Double
  var limit: Double?
}

/**
 * Spent-vs-limit bar. Colour marks state (under / close / over) and the text
 * beside it says the same thing, so colour is never the only channel.
 */
@JsName("BudgetMeter")
val BudgetMeter: FC<BudgetMeterProps> = FC { props ->
  val limit = props.limit?.takeIf { it > 0.0 }
  val remaining = limit?.let { it - props.spent }
  val usage = if (limit != null) props.spent / limit else 0.0

  val state = when {
    limit == null -> BudgetState.None
    remaining!! < 0 -> BudgetState.Over
    usage >= 0.8 -> BudgetState.Tight
    else -> BudgetState.Ok
  }

  div {
    className = ClassName("budget-meter")

    div {
      className = ClassName("budget-meter__row")
      span {
        className = ClassName("budget-meter__status")
        style = jso { color = Color(state.color) }
        +when (state) {
          BudgetState.None -> "Kein Limit gesetzt"
          BudgetState.Over -> "${fmtEurShort(abs(remaining!!))} über Budget"
          else -> "${fmtEurShort(remaining!!)} verfügbar"
        }
      }
      span {
        className = ClassName("budget-meter__value")
        +if (limit != null) {
          "${fmtEurShort(props.spent)} / ${fmtEurShort(limit)}"
        } else {
          fmtEurShort(props.spent)
        }
      }
    }

    div {
      className = ClassName("bar-track")
      div {
        className = ClassName("bar-fill")
        style = jso {
          width = (min(usage, 1.0) * 100).pct
          background = Color(if (limit == null) T.track else state.color)
        }
      }
    }
  }
}

private enum class BudgetState(val color: String) {
  Ok(T.good),
  Tight("#c98500"),
  Over(T.critical),
  None(T.textMuted),
}
