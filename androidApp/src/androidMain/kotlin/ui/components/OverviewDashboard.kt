package com.banking.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.banking.app.ui.format.fmtEur
import com.banking.app.ui.format.fmtDay
import com.banking.app.ui.theme.BankingAppTheme
import com.banking.app.ui.theme.T
import com.banking.app.ui.theme.hexColor
import com.banking.shared.data.Category
import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.Transaction

/** Mobile overview: real statement data, with an intentional first-run state. */
@Composable
fun OverviewDashboard(
    transactions: List<Transaction>,
    categories: List<Category>,
    period: String,
    onUpload: (Uri) -> Unit,
    onShowTransactions: () -> Unit,
    onShowBudgets: () -> Unit,
    hasImportedData: Boolean = transactions.isNotEmpty(),
) {
    val total = transactions.sumOf { it.amount }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF525CE5), Color(0xFF30258F))))
                .drawBehind {
                    drawCircle(Color.White.copy(alpha = .05f), size.width * .42f, Offset(size.width, 0f))
                    drawCircle(Color.White.copy(alpha = .04f), size.width * .35f, Offset(size.width * .9f, size.height))
                }
                .padding(24.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DEINE AUSGABEN", color = Color.White.copy(alpha = .8f), fontSize = 11.sp,
                    letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(22.dp))
            }
            Text(if (!hasImportedData) "Noch offen" else fmtEur(total), color = Color.White,
                fontSize = 36.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Text(if (!hasImportedData) "Dein Überblick beginnt mit dem ersten PDF." else period,
                color = Color.White.copy(alpha = .78f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("BudgetFlow", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${transactions.size} Buchungen", color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .14f)).padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Buchungen", "Alle Ausgaben", Modifier.weight(1f), onShowTransactions)
            QuickAction("Budgets", "Ziele festlegen", Modifier.weight(1f), onShowBudgets)
        }

        if (!hasImportedData) {
            SectionHeading("Alles beginnt hier")
            UploadPanel(onUpload = onUpload)
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = T.accent, modifier = Modifier.size(26.dp))
                Text("Importiere einen Kontoauszug und entdecke, wohin dein Geld fließt.",
                    color = T.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(start = 12.dp))
            }
        } else {
            if (transactions.isEmpty()) {
                Panel { EmptyHint("Keine Buchungen für diese Filter. Passe deine Auswahl oben links an.") }
            }
            SectionHeading("Deine Kategorien")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                val topCategories = transactions.groupBy { it.category }.entries
                    .sortedByDescending { entry -> entry.value.sumOf { it.amount } }.take(5)
                topCategories.forEach { entry ->
                    val category = categories.find { it.name == entry.key }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
                        Box(Modifier.size(54.dp).clip(CircleShape).background(hexColor(category?.color).copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                            Text(category?.icon ?: "•", fontSize = 24.sp)
                        }
                        Text(category?.label ?: entry.key, color = T.textSecondary, fontSize = 11.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                SectionHeading("Letzte Buchungen")
                TextButton(onClick = onShowTransactions) { Text("Alle anzeigen", fontSize = 12.sp) }
            }
            Column(Modifier.clip(RoundedCornerShape(22.dp)).background(T.surface)) {
                transactions.sortedByDescending { it.date }.take(3).forEach { tx ->
                    val category = categories.find { it.name == tx.category }
                    Row(Modifier.fillMaxWidth().clickable(onClick = onShowTransactions).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(hexColor(category?.color).copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                            Text(category?.icon ?: "•", fontSize = 20.sp)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(tx.description, color = T.text, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${category?.label ?: tx.category} · ${fmtDay(tx.date)}", color = T.textMuted, fontSize = 11.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                        }
                        Text(fmtEur(tx.amount), color = T.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.clip(RoundedCornerShape(18.dp)).background(T.accentSoft).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = T.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = T.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Icon(Icons.Default.ArrowForward, null, tint = T.accent, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = T.text)
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun EmptyOverviewPreview() {
    BankingAppTheme {
        Column(Modifier.background(T.bg).padding(20.dp)) {
            OverviewDashboard(emptyList(), CategoryMapper.getAllCategories(), "Alle Monate", {}, {}, {})
        }
    }
}