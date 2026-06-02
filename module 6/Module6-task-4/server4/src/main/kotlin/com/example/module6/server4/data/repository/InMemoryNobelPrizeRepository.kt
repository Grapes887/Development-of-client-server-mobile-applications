package com.example.module6.server4.data.repository

import com.example.module6.server4.domain.model.Laureate
import com.example.module6.server4.domain.model.NobelPrize
import com.example.module6.server4.domain.repository.NobelPrizeRepository

class InMemoryNobelPrizeRepository : NobelPrizeRepository {

    private val prizes = listOf(
        NobelPrize(
            awardYear = "2023",
            category = "physics",
            categoryTitle = "The Nobel Prize in Physics",
            detailLink = "https://www.nobelprize.org/prizes/physics/2023/summary/",
            laureates = listOf(
                Laureate(
                    id = "1001",
                    fullName = "Pierre Agostini",
                    portion = "1/3",
                    motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter"
                ),
                Laureate(
                    id = "1002",
                    fullName = "Ferenc Krausz",
                    portion = "1/3",
                    motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter"
                ),
                Laureate(
                    id = "1003",
                    fullName = "Anne L'Huillier",
                    portion = "1/3",
                    motivation = "for experimental methods that generate attosecond pulses of light for the study of electron dynamics in matter"
                )
            )
        ),
        NobelPrize(
            awardYear = "2023",
            category = "chemistry",
            categoryTitle = "The Nobel Prize in Chemistry",
            detailLink = "https://www.nobelprize.org/prizes/chemistry/2023/summary/",
            laureates = listOf(
                Laureate(
                    id = "1029",
                    fullName = "Moungi G. Bawendi",
                    portion = "1/3",
                    motivation = "for the discovery and synthesis of quantum dots"
                ),
                Laureate(
                    id = "1030",
                    fullName = "Louis E. Brus",
                    portion = "1/3",
                    motivation = "for the discovery and synthesis of quantum dots"
                ),
                Laureate(
                    id = "1031",
                    fullName = "Aleksey Yekimov",
                    portion = "1/3",
                    motivation = "for the discovery and synthesis of quantum dots"
                )
            )
        ),
        NobelPrize(
            awardYear = "2022",
            category = "peace",
            categoryTitle = "The Nobel Peace Prize",
            detailLink = "https://www.nobelprize.org/prizes/peace/2022/summary/",
            laureates = listOf(
                Laureate(
                    id = "1101",
                    fullName = "Ales Bialiatski",
                    portion = "1/3",
                    motivation = "for promoting the right to criticize power and protect the fundamental rights of citizens"
                ),
                Laureate(
                    id = "1102",
                    fullName = "Memorial",
                    portion = "1/3",
                    motivation = "for documenting war crimes, human rights abuses and the abuse of power"
                ),
                Laureate(
                    id = "1103",
                    fullName = "Center for Civil Liberties",
                    portion = "1/3",
                    motivation = "for advancing human rights and building living democracies"
                )
            )
        )
    )

    override fun getPrizes(): List<NobelPrize> = prizes

    override fun getPrize(year: String, category: String): NobelPrize? {
        return prizes.firstOrNull { prize ->
            prize.awardYear == year && prize.category.equals(category, ignoreCase = true)
        }
    }

    override fun getLaureates(year: String, category: String): List<Laureate> {
        return getPrize(year, category)?.laureates.orEmpty()
    }
}
