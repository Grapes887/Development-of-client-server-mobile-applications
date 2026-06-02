package com.example.module6.server4.domain.repository

import com.example.module6.server4.domain.model.Laureate
import com.example.module6.server4.domain.model.NobelPrize

interface NobelPrizeRepository {
    fun getPrizes(): List<NobelPrize>

    fun getPrize(year: String, category: String): NobelPrize?

    fun getLaureates(year: String, category: String): List<Laureate>
}
