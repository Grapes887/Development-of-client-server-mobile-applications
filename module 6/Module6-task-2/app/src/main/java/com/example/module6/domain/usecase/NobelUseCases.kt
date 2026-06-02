package com.example.module6.domain.usecase

import com.example.module6.domain.model.LaureateDetail
import com.example.module6.domain.model.LaureateListItem
import com.example.module6.domain.repository.NobelRepository

class GetLaureatesUseCase(
    private val repository: NobelRepository
) {
    suspend operator fun invoke(
        year: String?,
        categoryKey: String
    ): List<LaureateListItem> {
        return repository.getLaureates(year, categoryKey)
    }
}

class GetLaureateDetailUseCase(
    private val repository: NobelRepository
) {
    suspend operator fun invoke(summary: LaureateListItem): LaureateDetail {
        return repository.getLaureateDetail(summary)
    }
}
