package com.example.module6.server5.domain.usecase

import com.example.module6.server5.domain.model.ServerLaureate
import com.example.module6.server5.domain.model.ServerPrize
import com.example.module6.server5.domain.model.ServerUser
import com.example.module6.server5.domain.repository.PrizeRepository
import com.example.module6.server5.domain.repository.UserRepository
import com.example.module6.server5.security.PasswordHasher

class LoginUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(username: String, password: String): ServerUser? {
        val user = userRepository.findByUsername(username) ?: return null
        return if (PasswordHasher.verify(password, user.passwordHash)) user else null
    }
}

class GetProfileUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Int): ServerUser? = userRepository.findById(userId)
}

class GetPrizesUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(year: String?, category: String?): List<ServerPrize> {
        return prizeRepository.getPrizes(year, category)
    }
}

class GetPrizeUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(year: String, category: String): ServerPrize? {
        return prizeRepository.getPrize(year, category)
    }
}

class GetLaureatesByPrizeUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(year: String, category: String): List<ServerLaureate> {
        return prizeRepository.getLaureates(year, category)
    }
}

class GetLaureateDetailUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(laureateId: String): ServerLaureate? {
        return prizeRepository.getLaureateById(laureateId)
    }
}

class GetFavoritePrizesUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(userId: Int): List<ServerPrize> {
        return prizeRepository.getFavoritePrizes(userId)
    }
}

class AddFavoritePrizeUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(userId: Int, prizeId: Int): Boolean {
        return prizeRepository.addFavoritePrize(userId, prizeId)
    }
}

class RemoveFavoritePrizeUseCase(
    private val prizeRepository: PrizeRepository
) {
    operator fun invoke(userId: Int, prizeId: Int): Boolean {
        return prizeRepository.removeFavoritePrize(userId, prizeId)
    }
}
