package com.example.module6.server4.domain.usecase

import com.example.module6.server4.domain.model.DemoUser
import com.example.module6.server4.domain.model.Laureate
import com.example.module6.server4.domain.model.NobelPrize
import com.example.module6.server4.domain.repository.NobelPrizeRepository

class AuthenticateUserUseCase {
    private val demoUsers = listOf(
        DemoUser(username = "student", password = "student123"),
        DemoUser(username = "admin", password = "admin123")
    )

    operator fun invoke(username: String, password: String): Boolean {
        return demoUsers.any { user ->
            user.username == username && user.password == password
        }
    }
}

class GetPrizesUseCase(
    private val repository: NobelPrizeRepository
) {
    operator fun invoke(): List<NobelPrize> = repository.getPrizes()
}

class GetPrizeByYearCategoryUseCase(
    private val repository: NobelPrizeRepository
) {
    operator fun invoke(year: String, category: String): NobelPrize? {
        return repository.getPrize(year, category)
    }
}

class GetLaureatesByPrizeUseCase(
    private val repository: NobelPrizeRepository
) {
    operator fun invoke(year: String, category: String): List<Laureate> {
        return repository.getLaureates(year, category)
    }
}
