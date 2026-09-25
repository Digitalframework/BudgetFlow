package components

import com.banking.shared.data.AccountBalance
import com.banking.shared.data.SavingsCalculator
import design.T
import design.fmtDay
import design.fmtEur
import design.fmtMonth
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import utils.jso
import web.cssom.ClassName
import web.cssom.Color

external interface SavingsCardProps : Props {
  var balances: List<AccountBalance>

  /** Selected month ("yyyy-MM"); null compares up to the newest balance. */
  var month: String?
}

/**
 * "Wie viel wurde gespart?" — the oldest known account balance subtracted from
 * the balance at the end of the selected month.
 *
 * Every statement carries exactly one opening balance, so the card stays in its
 * empty state until a second statement is imported.
 */
@JsName("SavingsCard")
val SavingsCard: FC<SavingsCardProps> = FC { props ->
  val openings = SavingsCalculator.openingBalances(props.balances)
  val comparison = SavingsCalculator.compare(props.balances, props.month)

  Panel {
    title = "Ersparnis"
    extra = when (openings.size) {
      0 -> "Kein Kontostand erkannt"
      1 -> "1 Kontoauszug"
      else -> "${openings.size} Kontoauszüge"
    }
    children = Fragment.create {

      if (comparison == null) {
        div {
          className = ClassName("savings-empty")

          openings.firstOrNull()?.let { only ->
            if (openings.size == 1) {
              div {
                className = ClassName("savings-empty__balance")
                +"Kontostand ${balanceDate(only.date)}: ${fmtEur(only.amount)}"
              }
            }
          }

          div {
            className = ClassName("savings-empty__text")
            +when {
              openings.isEmpty() ->
                "Auf diesem Kontoauszug wurde kein „alter Kontostand\" gefunden. " +
                  "Der Vergleich braucht die Kontostandzeile des Auszugs."
              openings.size == 1 ->
                "Für den Vergleich werden zwei Kontoauszüge benötigt. " +
                  "Importiere einen Auszug aus einem anderen Monat, um zu sehen, " +
                  "wie viel dazugekommen ist."
              else ->
                "Für ${fmtMonth(props.month)} gibt es keinen Kontostand nach dem " +
                  "ältesten Auszug. Wähle einen späteren Monat."
            }
          }
        }
      } else {
        val saved = comparison.difference >= 0
        val accent = if (saved) T.good else T.critical

        div {
          className = ClassName("savings")

          div {
            className = ClassName("savings__value")
            style = jso { color = Color(accent) }
            +"${if (saved) "+" else "−"}${fmtEur(kotlin.math.abs(comparison.difference))}"
          }

          div {
            className = ClassName("savings__caption")
            +(
              (if (saved) "gespart von " else "weniger auf dem Konto von ") +
                "${fmtMonth(comparison.from.date.take(7))} bis " +
                fmtMonth(comparison.to.date.take(7))
              )
          }

          div {
            className = ClassName("savings__rail")

            BalancePoint {
              label = "Startkontostand"
              date = comparison.from.date
              amount = comparison.from.amount
            }

            span {
              className = ClassName("savings__arrow")
              +"→"
            }

            BalancePoint {
              label = "Endkontostand"
              date = comparison.to.date
              amount = comparison.to.amount
            }
          }

          if (props.month == null) {
            div {
              className = ClassName("savings__note")
              +"Wähle einen Monat im Verlauf, um bis dorthin zu rechnen."
            }
          }
        }
      }
    }
  }
}

/** "2025-12-30" → "30. Dez 2025" */
private fun balanceDate(iso: String): String = "${fmtDay(iso)} ${iso.take(4)}"

private external interface BalancePointProps : Props {
  var label: String
  var date: String
  var amount: Double
}

private val BalancePoint: FC<BalancePointProps> = FC { props ->
  div {
    className = ClassName("savings-point")
    div {
      className = ClassName("savings-point__label")
      +props.label
    }
    div {
      className = ClassName("savings-point__value")
      +fmtEur(props.amount)
    }
    div {
      className = ClassName("savings-point__date")
      +balanceDate(props.date)
    }
  }
}
