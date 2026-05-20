package com.banking.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.CategoryMapper
import data.TransactionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarFilters(
    isOpen: Boolean,
    currentFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(currentFilter.search) }
    var selectedCategory by remember { mutableStateOf(currentFilter.category) }

    if (isOpen) {
        ModalDrawerSheet(
            modifier = Modifier.width(280.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Divider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onFilterChange(currentFilter.copy(search = it))
                        },
                        label = { Text("Search") },
                        placeholder = { Text("Description...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Category Filter
                item {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = {
                            selectedCategory = "all"
                            onFilterChange(currentFilter.copy(category = "all"))
                        },
                        label = { Text("All") }
                    )
                }

                items(CategoryMapper.CATEGORIES.size) { index ->
                    val (key, category) = CategoryMapper.CATEGORIES.toList()[index]
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = {
                            selectedCategory = key
                            onFilterChange(currentFilter.copy(category = key))
                        },
                        label = { Text("${category.icon} ${category.label}") },
                        leadingIcon = if (selectedCategory == key) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}