package com.example.module6.data.repository

import com.example.module6.data.model.toDomain
import com.example.module6.data.remote.PicsumApiService
import com.example.module6.domain.model.PhotoItem
import com.example.module6.domain.repository.PhotoRepository
import java.io.OutputStream

class PhotoRepositoryImpl(
    private val apiService: PicsumApiService
) : PhotoRepository {

    override suspend fun getPhotos(): List<PhotoItem> {
        return apiService.getPhotos().map { dto -> dto.toDomain() }
    }

    override suspend fun downloadPhoto(photo: PhotoItem, outputStream: OutputStream) {
        apiService.downloadPhoto(photo.imageUrl).byteStream().use { inputStream ->
            outputStream.use { stream ->
                inputStream.copyTo(stream)
                stream.flush()
            }
        }
    }
}
