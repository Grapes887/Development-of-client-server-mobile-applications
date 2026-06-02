package com.example.module6.data.remote

import com.example.module6.data.model.LoginRequestDto
import com.example.module6.data.model.LoginResponseDto
import com.example.module6.data.model.UserDto
import com.example.module6.data.model.UsersResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class DummyJsonApiService(
    private val client: HttpClient
) {
    suspend fun login(username: String, password: String): LoginResponseDto {
        return client.post("auth/login") {
            setBody(LoginRequestDto(username = username, password = password))
        }.body()
    }

    suspend fun getUsers(token: String): UsersResponseDto {
        return client.get("users") {
            bearerAuth(token)
        }.body()
    }

    suspend fun getUser(id: Int, token: String): UserDto {
        return client.get("users/$id") {
            bearerAuth(token)
        }.body()
    }
}
