package com.banking.app.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banking.app.data.BudgetStore
import com.banking.app.pdf.PdfTextExtractor
import com.banking.app.ui.components.BudgetsPage
import com.banking.app.ui.components.CategoryBreakdown
import com.banking.app.ui.components.MonthlyTrend
import com.banking.app.ui.components.SidebarFilters
import com.banking.app.ui.components.StatTiles
import com.banking.app.ui.components.TopMerchants
import com.banking.app.ui.components.TransactionTable
import com.banking.app.ui.components.UploadPanel
import com.banking.app.ui.format.fmtMonth
import com.banking.app.ui.theme.T
import com.banking.shared.data.CategoryMapper
import com.banking.shared.data.TransactionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.BankStatementParser
import viewmodel.TransactionViewModel

private enum class View(val key: String, val label: String, val icon: ImageVector) {
    Overview("overview", "Übersicht", Icons.Default.PieChart),
    Budgets("budgets", "Budgets", Icons.Default.AccountBalanceWallet),
    Table("table", "Buchungen", Icons.Default.List),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingApp(viewModel: TransactionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val budgetStore = remember { BudgetStore(context) }
    var budgets by remember { mutableStateOf(budgetStore.load()) }

    // The filter lives here, not in the ViewModel: every chart needs the full
    // list to scope itself differently, exactly like the web app does.
    var filter by remember { mutableStateOf(TransactionFilter()) }
    var view by remember { mutableStateOf(View.Overview) }
    var parsing by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val categories = remember { CategoryMapper.getAllCategories() }

    val transactions = uiState.transactions

    // Month + search apply everywhere; the category filter only narrows the
    // transaction list, so the category chart keeps showing the full split.
    val scoped = transactions
        .filter { filter.month == null || it.date.startsWith(filter.month!!) }
        .filter {
            filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true)
        }

    val filteredTx = scoped.filter { filter.category == "all" || it.category == filter.category }

    // The trend chart is the month picker, so it must ignore the month filter
    // while still respecting search + category.
    val allMonths = transactions
        .filter {
            filter.search.isEmpty() || it.description.contains(filter.search, ignoreCase = true)
        }
        .filter { filter.category == "all" || it.category == filter.category }

    val hasData = transactions.isNotEmpty()

    val subtitle = listOfNotNull(
        if (filter.month != null) fmtMonth(filter.month) else "Alle Monate",
        if (filter.category != "all") {
            categories.find { it.name == filter.category }?.label ?: filter.category
        } else null,
        if (filter.search.isNotEmpty()) "„${filter.search}\"" else null,
    ).joinToString(" · ")

    val handleUpload: (Uri) -> Unit = { uri ->
        parsing = true
        scope.launch {
            val name = withContext(Dispatchers.IO) { PdfTextExtractor.displayName(context, uri) }
            val parsed = withContext(Dispatchers.IO) {
                runCatching {
                    BankStatementParser.parseTransactions(
                        PdfTextExtractor.extractLines(context, uri)
                    )
                }
            }
            parsing = false

            parsed.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) {
                        snackbarHostState.showSnackbar(
                            "Keine Transaktionen erkannt. Bitte prüfe das PDF-Format."
                        )
                    } else {
                        // The ViewModel categorises on insert.
                        viewModel.addTransactions(list)
                        viewModel.setPdfFileName(name)
                        snackbarHostState.showSnackbar(
                            "${list.size} Buchungen aus „$name\" importiert"
                        )
                    }
                },
                onFailure = {
                    snackbarHostState.showSnackbar("Fehler beim Lesen der PDF")
                },
            )
        }
    }

    // Nothing to filter once the data is gone.
    LaunchedEffect(hasData) {
        if (!hasData) filter = TransactionFilter()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = hasData,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = T.bg) {
                SidebarFilters(
                    transactions = transactions,
                    categories = categories,
                    filter = filter,
                    onFilterChange = { filter = it },
                    onClose = { scope.launch { drawerState.close() } },
                )
            }
        },
    ) {
        Scaffold(
            containerColor = T.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column(modifier = Modifier.background(T.bg)) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = T.bg,
                            titleContentColor = T.text,
                        ),
                        navigationIcon = {
                            if (hasData) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Filter",
                                        tint = T.textMuted,
                                    )
                                }
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = view.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = T.text,
                                )
                                Text(
                                    text = when {
                                        !hasData -> "Noch keine Daten"
                                        view == View.Budgets ->
                                            "${budgets.size} " +
                                                (if (budgets.size == 1) "Budget" else "Budgets") +
                                                " · " +
                                                (filter.month?.let { fmtMonth(it) }
                                                    ?: "Neuester Monat")
                                        else -> "${filteredTx.size} Buchungen · $subtitle"
                                    },
                                    fontSize = 12.sp,
                                    color = T.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        actions = {
                            UploadPanel(onUpload = handleUpload, compact = true)
                            if (hasData) {
                                IconButton(onClick = { showClearDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Alle Daten löschen",
                                        tint = T.critical,
                                    )
                                }
                            }
                        },
                    )

                    if (hasData) {
                        ViewSwitcher(
                            selected = view,
                            onSelect = { view = it },
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 12.dp,
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(T.border),
                    )
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when {
                    !hasData -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(modifier = Modifier.height(64.dp))
                            UploadPanel(onUpload = handleUpload)
                        }
                    }

                    view == View.Table -> {
                        TransactionTable(
                            transactions = filteredTx,
                            categories = categories,
                            onCategoryChange = { id, category ->
                                viewModel.updateCategory(id, category)
                            },
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 24.dp,
                            ),
                        )
                    }

                    view == View.Budgets -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        ) {
                            BudgetsPage(
                                transactions = transactions,
                                categories = categories,
                                budgets = budgets,
                                month = filter.month,
                                onSetBudget = { category, limit ->
                                    budgets = budgetStore.set(category, limit)
                                },
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            StatTiles(transactions = filteredTx, categories = categories)

                            CategoryBreakdown(
                                transactions = scoped,
                                categories = categories,
                                activeCategory = filter.category,
                                onSelectCategory = { filter = filter.copy(category = it) },
                            )

                            MonthlyTrend(
                                transactions = allMonths,
                                activeMonth = filter.month,
                                onSelectMonth = { filter = filter.copy(month = it) },
                            )

                            TopMerchants(
                                transactions = filteredTx,
                                categories = categories,
                                onSelectMerchant = { filter = filter.copy(search = it) },
                            )
                        }
                    }
                }

                if (parsing || uiState.loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(T.bg.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = T.accent)
                            Text(
                                text = "PDF wird analysiert…",
                                fontSize = 13.sp,
                                color = T.textSecondary,
                                modifier = Modifier.padding(top = 14.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = T.surfaceAlt,
            title = { Text("Alle Daten löschen?", color = T.text) },
            text = {
                Text(
                    "Alle importierten Transaktionen werden unwiderruflich entfernt.",
                    color = T.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    filter = TransactionFilter()
                    showClearDialog = false
                    scope.launch { snackbarHostState.showSnackbar("Daten gelöscht") }
                }) {
                    Text("Löschen", color = T.critical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Abbrechen", color = T.textSecondary)
                }
            },
        )
    }
}

/** The header's Segmented control from the web, as a Compose pill switcher. */
@Composable
private fun ViewSwitcher(
    selected: View,
    onSelect: (View) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(T.surfaceAlt)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        View.values().forEach { entry ->
            val active = entry == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) T.accent else Color.Transparent)
                    .clickable { onSelect(entry) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = if (active) Color.White else T.textMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = entry.label,
                    fontSize = 13.sp,
                    color = if (active) Color.White else T.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 7.dp),
                )
            }
        }
    }
}
