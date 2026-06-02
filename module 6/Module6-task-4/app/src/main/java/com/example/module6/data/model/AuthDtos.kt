package com.example.module6.data.model

data class LoginRequestDto(
    val username: String,
    val password: String
)

data class LoginResponseDto(
    val accessToken: String = "",
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val image: String = ""
)

data class UsersResponseDto(
    val users: List<UserDto> = emptyList()
)

data class UserDto(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val image: String = ""
)
