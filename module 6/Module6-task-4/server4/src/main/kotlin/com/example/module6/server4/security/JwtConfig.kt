package com.example.module6.server4.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "module6_super_secret_jwt_key_123456789"
    const val issuer = "module6-server4"
    const val audience = "module6-clients"
    const val realm = "module6-realm"
    private const val expirationMillis = 30 * 60 * 1000L

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(username: String): String {
        val expiresAt = Date(System.currentTimeMillis() + expirationMillis)
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}
