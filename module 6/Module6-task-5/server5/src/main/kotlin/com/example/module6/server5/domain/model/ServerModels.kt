package com.example.module6.server5.domain.model

data class ServerUser(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val role: String
)

data class ServerPrize(
    val id: Int,
    val awardYear: String,
    val category: String,
    val categoryTitle: String,
    val detailLink: String,
    val laureates: List<ServerLaureate>
)

data class ServerLaureate(
    val id: String,
    val prizeId: Int,
    val fullName: String,
    val portion: String,
    val motivation: String,
    val birthLocation: String,
    val portraitUrl: String?
)

data class SeedPrize(
    val awardYear: String,
    val category: String,
    val categoryTitle: String,
    val detailLink: String,
    val laureates: List<SeedLaureate>
) {
    companion object {
        val entries = listOf(
            SeedPrize(
                awardYear = "2023",
                category = "physics",
                categoryTitle = "The Nobel Prize in Physics",
                detailLink = "https://www.nobelprize.org/prizes/physics/2023/summary/",
                laureates = listOf(
                    SeedLaureate(
                        id = "1001",
                        fullName = "Pierre Agostini",
                        portion = "1/3",
                        motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter",
                        birthLocation = "Tunis, Tunisia"
                    ),
                    SeedLaureate(
                        id = "1002",
                        fullName = "Ferenc Krausz",
                        portion = "1/3",
                        motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter",
                        birthLocation = "Mór, Hungary"
                    ),
                    SeedLaureate(
                        id = "1003",
                        fullName = "Anne L'Huillier",
                        portion = "1/3",
                        motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter",
                        birthLocation = "Paris, France"
                    )
                )
            ),
            SeedPrize(
                awardYear = "2023",
                category = "chemistry",
                categoryTitle = "The Nobel Prize in Chemistry",
                detailLink = "https://www.nobelprize.org/prizes/chemistry/2023/summary/",
                laureates = listOf(
                    SeedLaureate(
                        id = "1029",
                        fullName = "Moungi G. Bawendi",
                        portion = "1/3",
                        motivation = "for the discovery and synthesis of quantum dots",
                        birthLocation = "Paris, France"
                    ),
                    SeedLaureate(
                        id = "1030",
                        fullName = "Louis E. Brus",
                        portion = "1/3",
                        motivation = "for the discovery and synthesis of quantum dots",
                        birthLocation = "Cleveland, OH, USA"
                    ),
                    SeedLaureate(
                        id = "1031",
                        fullName = "Aleksey Yekimov",
                        portion = "1/3",
                        motivation = "for the discovery and synthesis of quantum dots",
                        birthLocation = "Leningrad, USSR"
                    )
                )
            ),
            SeedPrize(
                awardYear = "2022",
                category = "peace",
                categoryTitle = "The Nobel Peace Prize",
                detailLink = "https://www.nobelprize.org/prizes/peace/2022/summary/",
                laureates = listOf(
                    SeedLaureate(
                        id = "1101",
                        fullName = "Ales Bialiatski",
                        portion = "1/3",
                        motivation = "for promoting the right to criticize power and protect the fundamental rights of citizens",
                        birthLocation = "Karelichy, Belarus"
                    ),
                    SeedLaureate(
                        id = "1102",
                        fullName = "Memorial",
                        portion = "1/3",
                        motivation = "for documenting war crimes, human rights abuses and the abuse of power",
                        birthLocation = "Moscow, Russia"
                    ),
                    SeedLaureate(
                        id = "1103",
                        fullName = "Center for Civil Liberties",
                        portion = "1/3",
                        motivation = "for advancing human rights and building living democracies",
                        birthLocation = "Kyiv, Ukraine"
                    )
                )
            )
        )
    }
}

data class SeedLaureate(
    val id: String,
    val fullName: String,
    val portion: String,
    val motivation: String,
    val birthLocation: String,
    val portraitUrl: String? = null
)
