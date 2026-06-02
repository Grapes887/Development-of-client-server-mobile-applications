package com.example.module6.data.repository

import com.example.module6.data.model.BirthPlaceDto
import com.example.module6.data.model.LaureateDetailDto
import com.example.module6.data.model.NobelPrizeDto
import com.example.module6.data.remote.NobelApiService
import com.example.module6.domain.model.LaureateCategoryOption
import com.example.module6.domain.model.LaureateDetail
import com.example.module6.domain.model.LaureateListItem
import com.example.module6.domain.repository.NobelRepository

class NobelRepositoryImpl(
    private val apiService: NobelApiService
) : NobelRepository {

    override suspend fun getLaureates(
        year: String?,
        categoryKey: String
    ): List<LaureateListItem> {
        return apiService.getPrizes(year)
            .nobelPrizes
            .flatMap { prize -> prize.toItems() }
            .filter { item ->
                categoryKey == LaureateCategoryOption.ALL.key || item.categoryKey == categoryKey
            }
            .sortedWith(
                compareByDescending<LaureateListItem> { item -> item.awardYear.toIntOrNull() ?: 0 }
                    .thenBy { item -> item.fullName }
            )
    }

    override suspend fun getLaureateDetail(summary: LaureateListItem): LaureateDetail {
        val detail = apiService.getLaureate(summary.id).firstOrNull()
            ?: error("Не удалось получить данные лауреата")
        val matchingPrize = detail.nobelPrizes.firstOrNull { prize ->
            prize.awardYear == summary.awardYear &&
                normalizeCategory(prize.category.en) == summary.categoryKey
        } ?: detail.nobelPrizes.firstOrNull()

        return LaureateDetail(
            id = summary.id,
            fullName = detail.fullName?.en ?: detail.knownName?.en ?: summary.fullName,
            awardYear = matchingPrize?.awardYear ?: summary.awardYear,
            categoryLabel = summary.categoryLabel,
            motivation = matchingPrize?.motivation?.en ?: summary.fullMotivation,
            birthLocation = formatBirthLocation(detail.birth?.place),
            portraitUrl = null
        )
    }

    private fun NobelPrizeDto.toItems(): List<LaureateListItem> {
        val categoryLabel = category.en.orEmpty()
        val categoryKey = normalizeCategory(categoryLabel)

        return laureates.map { laureate ->
            val fullName = laureate.fullName?.en ?: laureate.knownName?.en ?: "Неизвестный лауреат"
            val fullMotivation = laureate.motivation?.en ?: "Описание отсутствует"
            LaureateListItem(
                id = laureate.id,
                fullName = fullName,
                awardYear = awardYear,
                categoryKey = categoryKey,
                categoryLabel = categoryLabel,
                motivationPreview = fullMotivation.take(100),
                fullMotivation = fullMotivation
            )
        }
    }

    private fun normalizeCategory(rawCategory: String?): String {
        val value = rawCategory.orEmpty().lowercase()
        return when {
            "physics" in value -> LaureateCategoryOption.PHYSICS.key
            "chemistry" in value -> LaureateCategoryOption.CHEMISTRY.key
            "literature" in value -> LaureateCategoryOption.LITERATURE.key
            "peace" in value -> LaureateCategoryOption.PEACE.key
            "medicine" in value || "physiology" in value -> LaureateCategoryOption.MEDICINE.key
            "economic" in value -> LaureateCategoryOption.ECONOMICS.key
            else -> LaureateCategoryOption.ALL.key
        }
    }

    private fun formatBirthLocation(place: BirthPlaceDto?): String {
        return place?.locationString?.en
            ?: listOfNotNull(place?.city?.en, place?.country?.en).joinToString()
                .ifBlank { "Нет данных о месте рождения" }
    }
}
