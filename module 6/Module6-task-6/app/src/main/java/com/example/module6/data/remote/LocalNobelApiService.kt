package com.example.module6.data.remote

import com.example.module6.data.model.LaureateResponseDto
import com.example.module6.data.model.PrizeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class LocalNobelApiService(
    private val client: HttpClient
) {
    suspend fun getPrizes(year: String?, category: String?): List<PrizeResponseDto> {
        return client.get("prizes") {
            if (!year.isNullOrBlank()) parameter("year", year)
            if (!category.isNullOrBlank() && category != "all") parameter("category", category)
        }.body()
    }

    suspend fun getLaureate(id: String): LaureateResponseDto {
        return client.get("laureates/$id").body()
    }
}
