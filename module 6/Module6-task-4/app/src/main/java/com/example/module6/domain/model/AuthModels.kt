package com.example.module6.domain.model

data class AuthenticatedUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val imageUrl: String
) {
    val fullName: String
        get() = "$firstName $lastName"
}
