package com.example.module6.domain.repository

import com.example.module6.domain.model.LaureateDetail
import com.example.module6.domain.model.LaureateListItem

interface NobelRepository {
    suspend fun getLaureates(year: String?, categoryKey: String): List<LaureateListItem>

    suspend fun getLaureateDetail(summary: LaureateListItem): LaureateDetail
}
