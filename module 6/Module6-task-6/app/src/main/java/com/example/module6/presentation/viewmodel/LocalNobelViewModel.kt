package com.example.module6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.module6.domain.model.LaureateCategoryOption
import com.example.module6.domain.model.LaureateDetail
import com.example.module6.domain.model.LaureateListItem
import com.example.module6.domain.model.NobelFilters
import com.example.module6.domain.usecase.GetLaureateDetailUseCase
import com.example.module6.domain.usecase.GetLaureatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LaureatesUiState {
    data object Loading : LaureatesUiState
    data class Success(val items: List<LaureateListItem>) : LaureatesUiState
    data class Error(val message: String) : LaureatesUiState
}

sealed interface LaureateDetailUiState {
    data object Idle : LaureateDetailUiState
    data object Loading : LaureateDetailUiState
    data class Success(val detail: LaureateDetail) : LaureateDetailUiState
    data class Error(val message: String) : LaureateDetailUiState
}

class LocalNobelViewModel(
    private val getLaureatesUseCase: GetLaureatesUseCase,
    private val getLaureateDetailUseCase: GetLaureateDetailUseCase
) : ViewModel() {

    private val _filters = MutableStateFlow(NobelFilters())
    val filters: StateFlow<NobelFilters> = _filters.asStateFlow()

    private val _listState = MutableStateFlow<LaureatesUiState>(LaureatesUiState.Loading)
    val listState: StateFlow<LaureatesUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<LaureateDetailUiState>(LaureateDetailUiState.Idle)
    val detailState: StateFlow<LaureateDetailUiState> = _detailState.asStateFlow()

    private var currentLaureate: LaureateListItem? = null

    init {
        loadLaureates()
    }

    fun updateYear(year: String) {
        _filters.update { filters -> filters.copy(year = year.filter(Char::isDigit).take(4)) }
    }

    fun updateCategory(option: LaureateCategoryOption) {
        _filters.update { filters -> filters.copy(category = option) }
    }

    fun loadLaureates() {
        _listState.value = LaureatesUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val year = filters.value.year.takeIf { it.isNotBlank() }
                getLaureatesUseCase(year, filters.value.category.key)
            }.onSuccess { items ->
                _listState.value = LaureatesUiState.Success(items)
            }.onFailure { error ->
                _listState.value = LaureatesUiState.Error(
                    message = error.message ?: "Не удалось получить список лауреатов"
                )
            }
        }
    }

    fun openLaureate(item: LaureateListItem) {
        currentLaureate = item
        _detailState.value = LaureateDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getLaureateDetailUseCase(item)
            }.onSuccess { detail ->
                _detailState.value = LaureateDetailUiState.Success(detail)
            }.onFailure { error ->
                _detailState.value = LaureateDetailUiState.Error(
                    error.message ?: "Не удалось получить детали лауреата"
                )
            }
        }
    }

    fun reloadCurrentLaureate() {
        currentLaureate?.let(::openLaureate)
    }

    class Factory(
        private val getLaureatesUseCase: GetLaureatesUseCase,
        private val getLaureateDetailUseCase: GetLaureateDetailUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocalNobelViewModel(
                getLaureatesUseCase = getLaureatesUseCase,
                getLaureateDetailUseCase = getLaureateDetailUseCase
            ) as T
        }
    }
}
