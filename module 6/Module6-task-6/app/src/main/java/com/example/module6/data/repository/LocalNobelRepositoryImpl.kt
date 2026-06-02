package com.example.module6.data.repository

import com.example.module6.data.model.PrizeResponseDto
import com.example.module6.data.remote.LocalNobelApiService
import com.example.module6.domain.model.LaureateDetail
import com.example.module6.domain.model.LaureateListItem
import com.example.module6.domain.repository.NobelRepository

class LocalNobelRepositoryImpl(
    private val apiService: LocalNobelApiService
) : NobelRepository {

    override suspend fun getLaureates(
        year: String?,
        categoryKey: String
    ): List<LaureateListItem> {
        return apiService.getPrizes(year, categoryKey)
            .flatMap { prize -> prize.toItems() }
            .sortedWith(
                compareByDescending<LaureateListItem> { item -> item.awardYear.toIntOrNull() ?: 0 }
                    .thenBy { item -> item.fullName }
            )
    }

    override suspend fun getLaureateDetail(summary: LaureateListItem): LaureateDetail {
        val detail = apiService.getLaureate(summary.id)
        return LaureateDetail(
            id = detail.id,
            fullName = detail.fullName,
            awardYear = summary.awardYear,
            categoryLabel = summary.categoryLabel,
            motivation = detail.motivation,
            birthLocation = detail.birthLocation,
            portraitUrl = detail.portraitUrl
        )
    }
}

private fun PrizeResponseDto.toItems(): List<LaureateListItem> {
    return laureates.map { laureate ->
        LaureateListItem(
            id = laureate.id,
            fullName = laureate.fullName,
            awardYear = awardYear,
            categoryKey = category,
            categoryLabel = categoryTitle,
            motivationPreview = laureate.motivation.take(100),
            fullMotivation = laureate.motivation
        )
    }
}
