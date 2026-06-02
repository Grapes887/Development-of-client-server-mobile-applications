package com.example.module6.domain.repository

import com.example.module6.domain.model.AuthenticatedUser

interface AuthRepository {
    suspend fun getSavedToken(): String?

    suspend fun login(username: String, password: String)

    suspend fun getUsers(): List<AuthenticatedUser>

    suspend fun getUserDetail(id: Int): AuthenticatedUser

    suspend fun logout()
}
