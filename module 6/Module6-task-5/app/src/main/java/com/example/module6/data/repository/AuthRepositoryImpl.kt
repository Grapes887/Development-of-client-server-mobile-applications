package com.example.module6.data.repository

import com.example.module6.data.model.UserDto
import com.example.module6.data.preferences.AuthTokenDataStore
import com.example.module6.data.remote.DummyJsonApiService
import com.example.module6.domain.model.AuthenticatedUser
import com.example.module6.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: DummyJsonApiService,
    private val tokenStore: AuthTokenDataStore
) : AuthRepository {

    override suspend fun getSavedToken(): String? {
        return tokenStore.readToken()
    }

    override suspend fun login(username: String, password: String) {
        val response = apiService.login(username, password)
        tokenStore.saveToken(response.accessToken)
    }

    override suspend fun getUsers(): List<AuthenticatedUser> {
        val token = requireToken()
        return apiService.getUsers(token).users.map { dto -> dto.toDomain() }
    }

    override suspend fun getUserDetail(id: Int): AuthenticatedUser {
        val token = requireToken()
        return apiService.getUser(id, token).toDomain()
    }

    override suspend fun logout() {
        tokenStore.clearToken()
    }

    private suspend fun requireToken(): String {
        return tokenStore.readToken() ?: error("Пользователь не авторизован")
    }
}

private fun UserDto.toDomain(): AuthenticatedUser {
    return AuthenticatedUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        imageUrl = image
    )
}
