package com.example.module6.data.remote

import com.example.module6.data.model.LoginRequestDto
import com.example.module6.data.model.LoginResponseDto
import com.example.module6.data.model.UserDto
import com.example.module6.data.model.UsersResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class DummyJsonApiService(
    private val client: HttpClient
) {
    suspend fun login(username: String, password: String): LoginResponseDto {
        val response: HttpResponse = client.post("auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(LoginRequestDto(username = username, password = password))
        }
        if (response.status.value !in 200..299) {
            error("Неверный username или password")
        }
        return response.body<LoginResponseDto>().also { body ->
            if (body.accessToken.isBlank()) {
                error("Неверный username или password")
            }
        }
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
