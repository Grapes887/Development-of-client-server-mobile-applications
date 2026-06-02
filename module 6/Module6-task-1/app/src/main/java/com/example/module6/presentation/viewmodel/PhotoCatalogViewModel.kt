package com.example.module6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.module6.domain.model.PhotoItem
import com.example.module6.domain.usecase.DownloadPhotoUseCase
import com.example.module6.domain.usecase.GetPhotosUseCase
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PhotoListUiState {
    data object Loading : PhotoListUiState

    data class Success(
        val photos: List<PhotoItem>
    ) : PhotoListUiState

    data class Error(
        val message: String
    ) : PhotoListUiState
}

sealed interface DownloadState {
    data object Idle : DownloadState

    data object InProgress : DownloadState

    data class Success(
        val message: String
    ) : DownloadState

    data class Error(
        val message: String
    ) : DownloadState
}

class PhotoCatalogViewModel(
    private val getPhotosUseCase: GetPhotosUseCase,
    private val downloadPhotoUseCase: DownloadPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoListUiState>(PhotoListUiState.Loading)
    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<PhotoItem?>(null)
    val selectedPhoto: StateFlow<PhotoItem?> = _selectedPhoto.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        _uiState.value = PhotoListUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getPhotosUseCase()
            }.onSuccess { photos ->
                _uiState.value = PhotoListUiState.Success(photos)
                if (_selectedPhoto.value == null && photos.isNotEmpty()) {
                    _selectedPhoto.value = photos.first()
                }
            }.onFailure { error ->
                _uiState.value = PhotoListUiState.Error(
                    message = error.message ?: "Не удалось загрузить фотографии"
                )
            }
        }
    }

    fun selectPhoto(photo: PhotoItem) {
        _selectedPhoto.value = photo
    }

    fun downloadSelectedPhoto(outputStreamProvider: () -> OutputStream?) {
        val photo = _selectedPhoto.value ?: return
        _downloadState.value = DownloadState.InProgress
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val outputStream = checkNotNull(outputStreamProvider()) {
                    "Не удалось открыть файл для записи"
                }
                downloadPhotoUseCase(photo, outputStream)
            }.onSuccess {
                _downloadState.value = DownloadState.Success("Фото сохранено в Downloads")
            }.onFailure { error ->
                _downloadState.value = DownloadState.Error(
                    error.message ?: "Не удалось скачать фото"
                )
            }
        }
    }

    fun clearDownloadState() {
        _downloadState.update { DownloadState.Idle }
    }

    class Factory(
        private val getPhotosUseCase: GetPhotosUseCase,
        private val downloadPhotoUseCase: DownloadPhotoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotoCatalogViewModel(
                getPhotosUseCase = getPhotosUseCase,
                downloadPhotoUseCase = downloadPhotoUseCase
            ) as T
        }
    }
}
