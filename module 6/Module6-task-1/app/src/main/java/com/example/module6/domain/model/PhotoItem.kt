package com.example.module6.domain.model

data class PhotoItem(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val detailUrl: String,
    val imageUrl: String,
    val thumbnailUrl: String
) {
    val readableSize: String
        get() = "$width × $height"

    val fileName: String
        get() = "picsum_$id.jpg"
}
