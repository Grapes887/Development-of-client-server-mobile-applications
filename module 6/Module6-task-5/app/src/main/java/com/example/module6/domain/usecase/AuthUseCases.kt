package com.example.module6.domain.usecase

import com.example.module6.domain.model.AuthenticatedUser
import com.example.module6.domain.repository.AuthRepository

class GetSavedTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): String? = repository.getSavedToken()
}

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String) {
        repository.login(username, password)
    }
}

class GetUsersUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): List<AuthenticatedUser> = repository.getUsers()
}

class GetUserDetailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(id: Int): AuthenticatedUser = repository.getUserDetail(id)
}

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
