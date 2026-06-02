package com.example.module6.data.model

import com.example.module6.domain.model.PhotoItem

data class PhotoDto(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
)

fun PhotoDto.toDomain(): PhotoItem {
    return PhotoItem(
        id = id,
        author = author,
        width = width,
        height = height,
        detailUrl = url,
        imageUrl = download_url,
        thumbnailUrl = "https://picsum.photos/id/$id/800/800"
    )
}
