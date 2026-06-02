package com.example.module6.domain.usecase

import com.example.module6.domain.model.PhotoItem
import com.example.module6.domain.repository.PhotoRepository
import java.io.OutputStream

class GetPhotosUseCase(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(): List<PhotoItem> {
        return repository.getPhotos()
    }
}

class DownloadPhotoUseCase(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(photo: PhotoItem, outputStream: OutputStream) {
        repository.downloadPhoto(photo, outputStream)
    }
}
