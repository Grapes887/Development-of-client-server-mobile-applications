package com.example.module6.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.module6.domain.model.LaureateCategoryOption
import com.example.module6.domain.model.LaureateListItem
import com.example.module6.domain.model.NobelFilters
import com.example.module6.presentation.viewmodel.LaureatesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaureatesScreen(
    filters: NobelFilters,
    listState: LaureatesUiState,
    onYearChange: (String) -> Unit,
    onCategorySelected: (LaureateCategoryOption) -> Unit,
    onApplyFilters: () -> Unit,
    onRetry: () -> Unit,
    onLaureateClick: (LaureateListItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лауреаты с локального API") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            FiltersSection(
                filters = filters,
                onYearChange = onYearChange,
                onCategorySelected = onCategorySelected,
                onApplyFilters = onApplyFilters
            )
            Spacer(modifier = Modifier.height(14.dp))
            when (listState) {
                LaureatesUiState.Loading -> LoadingBlock()
                is LaureatesUiState.Error -> ErrorBlock(listState.message, onRetry)
                is LaureatesUiState.Success -> LaureatesList(listState.items, onLaureateClick)
            }
        }
    }
}

@Composable
private fun FiltersSection(
    filters: NobelFilters,
    onYearChange: (String) -> Unit,
    onCategorySelected: (LaureateCategoryOption) -> Unit,
    onApplyFilters: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FilterAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Фильтры", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = filters.year,
                onValueChange = onYearChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Год премии") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Категория: ${filters.category.title}")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    LaureateCategoryOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.title) },
                            onClick = {
                                onCategorySelected(option)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onApplyFilters, modifier = Modifier.fillMaxWidth()) {
                Text("Применить")
            }
        }
    }
}

@Composable
private fun LaureatesList(
    items: List<LaureateListItem>,
    onLaureateClick: (LaureateListItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyBlock()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { item -> item.id + item.awardYear + item.categoryKey }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaureateClick(item) },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${item.awardYear} • ${item.categoryLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.motivationPreview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBlock(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@Composable
private fun EmptyBlock() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "По выбранным фильтрам ничего не найдено", textAlign = TextAlign.Center)
    }
}
