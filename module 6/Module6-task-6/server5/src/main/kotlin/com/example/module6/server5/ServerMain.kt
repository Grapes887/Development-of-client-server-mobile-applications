package com.example.module6.server5

import com.example.module6.server5.data.database.DatabaseFactory
import com.example.module6.server5.data.repository.JdbcPrizeRepository
import com.example.module6.server5.data.repository.JdbcUserRepository
import com.example.module6.server5.domain.usecase.AddFavoritePrizeUseCase
import com.example.module6.server5.domain.usecase.GetFavoritePrizesUseCase
import com.example.module6.server5.domain.usecase.GetLaureateDetailUseCase
import com.example.module6.server5.domain.usecase.GetLaureatesByPrizeUseCase
import com.example.module6.server5.domain.usecase.GetPrizeUseCase
import com.example.module6.server5.domain.usecase.GetPrizesUseCase
import com.example.module6.server5.domain.usecase.GetProfileUseCase
import com.example.module6.server5.domain.usecase.LoginUserUseCase
import com.example.module6.server5.domain.usecase.RemoveFavoritePrizeUseCase
import com.example.module6.server5.presentation.configureRouting
import com.example.module6.server5.security.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson

fun main() {
    embeddedServer(Netty, host = "0.0.0.0", port = 8081) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val dataSource = DatabaseFactory.createDataSource()
    DatabaseFactory.initDatabase(dataSource)

    val userRepository = JdbcUserRepository(dataSource)
    val prizeRepository = JdbcPrizeRepository(dataSource)

    install(CallLogging)
    install(ContentNegotiation) {
        gson()
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val userId = credential.payload.getClaim("userId").asInt()
                if (username.isNullOrBlank() || userId == null) {
                    null
                } else {
                    io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                }
            }
        }
    }

    configureRouting(
        loginUserUseCase = LoginUserUseCase(userRepository),
        getPrizesUseCase = GetPrizesUseCase(prizeRepository),
        getPrizeUseCase = GetPrizeUseCase(prizeRepository),
        getLaureatesByPrizeUseCase = GetLaureatesByPrizeUseCase(prizeRepository),
        getLaureateDetailUseCase = GetLaureateDetailUseCase(prizeRepository),
        getProfileUseCase = GetProfileUseCase(userRepository),
        getFavoritePrizesUseCase = GetFavoritePrizesUseCase(prizeRepository),
        addFavoritePrizeUseCase = AddFavoritePrizeUseCase(prizeRepository),
        removeFavoritePrizeUseCase = RemoveFavoritePrizeUseCase(prizeRepository)
    )
}
