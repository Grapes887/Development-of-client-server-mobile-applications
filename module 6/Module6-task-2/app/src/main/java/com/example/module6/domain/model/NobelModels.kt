package com.example.module6.domain.model

data class LaureateListItem(
    val id: String,
    val fullName: String,
    val awardYear: String,
    val categoryKey: String,
    val categoryLabel: String,
    val motivationPreview: String,
    val fullMotivation: String
)

data class LaureateDetail(
    val id: String,
    val fullName: String,
    val awardYear: String,
    val categoryLabel: String,
    val motivation: String,
    val birthLocation: String,
    val portraitUrl: String?
)

data class NobelFilters(
    val year: String = "2023",
    val category: LaureateCategoryOption = LaureateCategoryOption.ALL
)

data class LaureateCategoryOption(
    val key: String,
    val title: String
) {
    companion object {
        val ALL = LaureateCategoryOption("all", "Все категории")
        val PHYSICS = LaureateCategoryOption("physics", "Physics")
        val CHEMISTRY = LaureateCategoryOption("chemistry", "Chemistry")
        val LITERATURE = LaureateCategoryOption("literature", "Literature")
        val PEACE = LaureateCategoryOption("peace", "Peace")
        val MEDICINE = LaureateCategoryOption("medicine", "Medicine")
        val ECONOMICS = LaureateCategoryOption("economics", "Economics")

        val entries = listOf(
            ALL,
            PHYSICS,
            CHEMISTRY,
            LITERATURE,
            PEACE,
            MEDICINE,
            ECONOMICS
        )
    }
}
