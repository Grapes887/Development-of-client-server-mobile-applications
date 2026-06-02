package com.example.module6.data.remote

import com.example.module6.data.model.LaureateDetailDto
import com.example.module6.data.model.NobelPrizesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NobelApiService(
    private val client: HttpClient
) {
    suspend fun getPrizes(year: String?): NobelPrizesResponseDto {
        return client.get("nobelPrizes") {
            parameter("limit", 25)
            parameter("offset", 0)
            if (!year.isNullOrBlank()) {
                parameter("nobelPrizeYear", year)
            }
        }.body()
    }

    suspend fun getLaureate(id: String): List<LaureateDetailDto> {
        return client.get("laureate/$id").body()
    }
}
