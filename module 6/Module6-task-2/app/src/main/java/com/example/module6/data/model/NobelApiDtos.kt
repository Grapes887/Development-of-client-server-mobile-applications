package com.example.module6.data.model

data class LocalizedTextDto(
    val en: String? = null
)

data class NobelPrizesResponseDto(
    val nobelPrizes: List<NobelPrizeDto> = emptyList()
)

data class NobelPrizeDto(
    val awardYear: String = "",
    val category: LocalizedTextDto = LocalizedTextDto(),
    val laureates: List<PrizeLaureateDto> = emptyList()
)

data class PrizeLaureateDto(
    val id: String = "",
    val fullName: LocalizedTextDto? = null,
    val knownName: LocalizedTextDto? = null,
    val motivation: LocalizedTextDto? = null
)

data class LaureateDetailDto(
    val id: String = "",
    val fullName: LocalizedTextDto? = null,
    val knownName: LocalizedTextDto? = null,
    val fileName: String? = null,
    val birth: BirthDto? = null,
    val nobelPrizes: List<LaureatePrizeDto> = emptyList()
)

data class BirthDto(
    val place: BirthPlaceDto? = null
)

data class BirthPlaceDto(
    val locationString: LocalizedTextDto? = null,
    val city: LocalizedTextDto? = null,
    val country: LocalizedTextDto? = null
)

data class LaureatePrizeDto(
    val awardYear: String = "",
    val category: LocalizedTextDto = LocalizedTextDto(),
    val motivation: LocalizedTextDto? = null
)
