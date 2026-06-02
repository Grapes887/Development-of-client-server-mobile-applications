package com.example.module6.data.model

data class PrizeResponseDto(
    val id: Int = 0,
    val awardYear: String = "",
    val category: String = "",
    val categoryTitle: String = "",
    val detailLink: String = "",
    val laureates: List<LaureateResponseDto> = emptyList()
)

data class LaureateResponseDto(
    val id: String = "",
    val fullName: String = "",
    val portion: String = "",
    val motivation: String = "",
    val birthLocation: String = "",
    val portraitUrl: String? = null
)
