package com.example.module6.domain.repository

import com.example.module6.domain.model.PhotoItem
import java.io.OutputStream

interface PhotoRepository {
    suspend fun getPhotos(): List<PhotoItem>

    suspend fun downloadPhoto(photo: PhotoItem, outputStream: OutputStream)
}
