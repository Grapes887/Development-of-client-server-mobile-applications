package com.example.module6.server4

import com.example.module6.server4.data.repository.InMemoryNobelPrizeRepository
import com.example.module6.server4.domain.usecase.AuthenticateUserUseCase
import com.example.module6.server4.domain.usecase.GetLaureatesByPrizeUseCase
import com.example.module6.server4.domain.usecase.GetPrizeByYearCategoryUseCase
import com.example.module6.server4.domain.usecase.GetPrizesUseCase
import com.example.module6.server4.presentation.configureRouting
import com.example.module6.server4.security.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0"
    ) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val repository = InMemoryNobelPrizeRepository()

    install(CallLogging)
    install(ContentNegotiation) {
        gson()
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNullOrBlank()) {
                    null
                } else {
                    io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                }
            }
        }
    }

    configureRouting(
        authenticateUserUseCase = AuthenticateUserUseCase(),
        getPrizesUseCase = GetPrizesUseCase(repository),
        getPrizeByYearCategoryUseCase = GetPrizeByYearCategoryUseCase(repository),
        getLaureatesByPrizeUseCase = GetLaureatesByPrizeUseCase(repository)
    )
}
