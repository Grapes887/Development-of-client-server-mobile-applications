package com.example.module6.server5.security

import java.security.MessageDigest

object PasswordHasher {
    fun hash(rawPassword: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(rawPassword.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    fun verify(rawPassword: String, hashedPassword: String): Boolean {
        return hash(rawPassword) == hashedPassword
    }
}
