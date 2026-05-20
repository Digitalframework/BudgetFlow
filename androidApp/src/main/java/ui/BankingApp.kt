package com.banking.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.banking.app.ui.components.SidebarFilters
import com.banking.app.ui.components.SummaryCards
import com.banking.app.ui.components.TransactionTable
import com.banking.app.ui.components.UploadPanel
import com.banking.app.ui.components.CategoryBreakdown
import data.TransactionFilter
import viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingApp(viewModel: TransactionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var sidebarOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banking App") },
                navigationIcon = {
                    IconButton(onClick = { sidebarOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sidebar Filters
            SidebarFilters(
                isOpen = sidebarOpen,
                currentFilter = uiState.filter,
                onFilterChange = { viewModel.setFilter(it) },
                onClose = { sidebarOpen = false }
            )

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Upload Panel
                UploadPanel(
                    onFileSelected = { file ->
                        viewModel.setPdfFileName(file.name)
                        // PDF parsing would be triggered here
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Cards
                SummaryCards(
                    transactions = uiState.transactions,
                    categories = data.CategoryMapper.getAllCategories()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Breakdown
                CategoryBreakdown(
                    transactions = uiState.transactions,
                    categories = data.CategoryMapper.getAllCategories()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Table
                TransactionTable(
                    transactions = uiState.transactions,
                    categories = data.CategoryMapper.getAllCategories(),
                    onCategoryChange = { id, category ->
                        viewModel.updateCategory(id, category)
                    }
                )
            }
        }
    }
}