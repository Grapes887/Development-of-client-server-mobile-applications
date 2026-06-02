package com.example.module6.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.module6.presentation.ui.screen.LaureateDetailScreen
import com.example.module6.presentation.ui.screen.LaureatesScreen
import com.example.module6.presentation.viewmodel.NobelViewModel

private const val LIST_ROUTE = "laureates"
private const val DETAIL_ROUTE = "laureate_detail"

@Composable
fun NobelNavGraph(
    viewModel: NobelViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = LIST_ROUTE,
        modifier = modifier
    ) {
        composable(LIST_ROUTE) {
            LaureatesScreen(
                filters = filters,
                listState = listState,
                onYearChange = viewModel::updateYear,
                onCategorySelected = viewModel::updateCategory,
                onApplyFilters = viewModel::loadLaureates,
                onRetry = viewModel::loadLaureates,
                onLaureateClick = { item ->
                    viewModel.openLaureate(item)
                    navController.navigate(DETAIL_ROUTE)
                }
            )
        }
        composable(DETAIL_ROUTE) {
            LaureateDetailScreen(
                detailState = detailState,
                onBack = { navController.popBackStack() },
                onRetry = viewModel::reloadCurrentLaureate
            )
        }
    }
}
