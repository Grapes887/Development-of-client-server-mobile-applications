package com.example.module6.server4.domain.model

data class Laureate(
    val id: String,
    val fullName: String,
    val portion: String,
    val motivation: String,
    val portraitUrl: String? = null
)

data class NobelPrize(
    val awardYear: String,
    val category: String,
    val categoryTitle: String,
    val detailLink: String,
    val laureates: List<Laureate>
)

data class DemoUser(
    val username: String,
    val password: String
)
