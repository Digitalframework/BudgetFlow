package com.banking.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.ui.format.fmtDay
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.shared.data.AccountBalance
import com.banking.shared.data.SavingsCalculator
import kotlin.math.abs

/**
 * "Wie viel wurde gespart?" — the oldest known account balance subtracted from
 * the balance at the end of the selected month.
 *
 * Every statement carries exactly one opening balance, so the card stays in its
 * empty state until a second statement is imported.
 */
@Composable
fun SavingsCard(
    balances: List<AccountBalance>,
    month: String?,
    modifier: Modifier = Modifier,
) {
    val openings = SavingsCalculator.openingBalances(balances)
    val comparison = SavingsCalculator.compare(balances, month)

    Panel(
        modifier = modifier,
        title = "Ersparnis",
        extra = when (openings.size) {
            0 -> "Kein Kontostand erkannt"
            1 -> "1 Kontoauszug"
            else -> "${openings.size} Kontoauszüge"
        },
    ) {
        if (comparison == null) {
            openings.singleOrNull()?.let { only ->
                Text(
                    text = "Kontostand ${balanceDate(only.date)}: " + fmtEur(only.amount),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = T.text,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                text = when {
                    openings.isEmpty() ->
                        "Auf diesem Kontoauszug wurde kein „alter Kontostand\" gefunden. " +
                            "Der Vergleich braucht die Kontostandzeile des Auszugs."
                    openings.size == 1 ->
                        "Für den Vergleich werden zwei Kontoauszüge benötigt. " +
                            "Importiere einen Auszug aus einem anderen Monat, um zu sehen, " +
                            "wie viel dazugekommen ist."
                    else ->
                        "Für ${fmtMonth(month)} gibt es keinen Kontostand nach dem " +
                            "ältesten Auszug. Wähle einen späteren Monat."
                },
                fontSize = 13.sp,
                color = T.textSecondary,
            )
        } else {
            val saved = comparison.difference >= 0

            Text(
                text = (if (saved) "+" else "−") + fmtEur(abs(comparison.difference)),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = if (saved) T.good else T.critical,
            )
            Text(
                text = (if (saved) "gespart von " else "weniger auf dem Konto von ") +
                    fmtMonth(comparison.from.date.take(7)) + " bis " +
                    fmtMonth(comparison.to.date.take(7)),
                fontSize = 13.sp,
                color = T.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BalancePoint(label = "Startkontostand", balance = comparison.from)
                Text(text = "→", fontSize = 15.sp, color = T.textMuted)
                BalancePoint(label = "Endkontostand", balance = comparison.to)
            }

            if (month == null) {
                Text(
                    text = "Tippe im Verlauf auf einen Monat, um bis dorthin zu rechnen.",
                    fontSize = 12.sp,
                    color = T.textMuted,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun RowScope.BalancePoint(label: String, balance: AccountBalance) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(T.surfaceAlt)
            .border(1.dp, T.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = T.textMuted,
        )
        Text(
            text = fmtEur(balance.amount),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = T.text,
            modifier = Modifier.padding(top = 5.dp),
        )
        Text(
            text = balanceDate(balance.date),
            fontSize = 12.sp,
            color = T.textSecondary,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/** "2025-12-30" → "30. Dez 2025" */
private fun balanceDate(iso: String): String = "${fmtDay(iso)} ${iso.take(4)}"
