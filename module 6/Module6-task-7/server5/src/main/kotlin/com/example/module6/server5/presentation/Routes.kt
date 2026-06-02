package com.example.module6.server5.presentation

import com.example.module6.server5.domain.model.ServerLaureate
import com.example.module6.server5.domain.model.ServerPrize
import com.example.module6.server5.domain.model.ServerUser
import com.example.module6.server5.domain.usecase.AddFavoritePrizeUseCase
import com.example.module6.server5.domain.usecase.GetFavoritePrizesUseCase
import com.example.module6.server5.domain.usecase.GetLaureateDetailUseCase
import com.example.module6.server5.domain.usecase.GetLaureatesByPrizeUseCase
import com.example.module6.server5.domain.usecase.GetPrizeUseCase
import com.example.module6.server5.domain.usecase.GetPrizesUseCase
import com.example.module6.server5.domain.usecase.GetProfileUseCase
import com.example.module6.server5.domain.usecase.LoginUserUseCase
import com.example.module6.server5.domain.usecase.RemoveFavoritePrizeUseCase
import com.example.module6.server5.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

data class LoginResponse(
    val token: String,
    val expiresInMinutes: Int
)

data class ProfileResponse(
    val id: Int,
    val username: String,
    val role: String
)

fun Application.configureRouting(
    loginUserUseCase: LoginUserUseCase,
    getPrizesUseCase: GetPrizesUseCase,
    getPrizeUseCase: GetPrizeUseCase,
    getLaureatesByPrizeUseCase: GetLaureatesByPrizeUseCase,
    getLaureateDetailUseCase: GetLaureateDetailUseCase,
    getProfileUseCase: GetProfileUseCase,
    getFavoritePrizesUseCase: GetFavoritePrizesUseCase,
    addFavoritePrizeUseCase: AddFavoritePrizeUseCase,
    removeFavoritePrizeUseCase: RemoveFavoritePrizeUseCase
) {
    routing {
        swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml")

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = loginUserUseCase(request.username, request.password)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid credentials"))
                return@post
            }

            call.respond(
                LoginResponse(
                    token = JwtConfig.generateToken(user.id, user.username),
                    expiresInMinutes = 30
                )
            )
        }

        get("/prizes") {
            val year = call.request.queryParameters["year"]
            val category = call.request.queryParameters["category"]
            call.respond(getPrizesUseCase(year, category).map(::toPrizeResponse))
        }

        get("/prizes/{year}/{category}") {
            val year = call.parameters["year"]
            val category = call.parameters["category"]
            if (year == null || category == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing year or category"))
                return@get
            }
            val prize = getPrizeUseCase(year, category)
            if (prize == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Prize not found"))
                return@get
            }
            call.respond(toPrizeResponse(prize))
        }

        get("/prizes/{year}/{category}/laureates") {
            val year = call.parameters["year"]
            val category = call.parameters["category"]
            if (year == null || category == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing year or category"))
                return@get
            }
            val laureates = getLaureatesByPrizeUseCase(year, category)
            if (laureates.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Laureates not found"))
                return@get
            }
            call.respond(laureates.map(::toLaureateResponse))
        }

        get("/laureates/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing laureate id"))
                return@get
            }
            val laureate = getLaureateDetailUseCase(id)
            if (laureate == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Laureate not found"))
                return@get
            }
            call.respond(toLaureateResponse(laureate))
        }

        authenticate("auth-jwt") {
            get("/users/me") {
                val principal = call.principalOrError() ?: return@get
                val user = getProfileUseCase(principal.userId)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "User not found"))
                    return@get
                }
                call.respond(user.toProfileResponse())
            }

            get("/users/me/prizes") {
                val principal = call.principalOrError() ?: return@get
                call.respond(getFavoritePrizesUseCase(principal.userId).map(::toPrizeResponse))
            }

            post("/users/me/prizes/{prizeId}") {
                val principal = call.principalOrError() ?: return@post
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                if (prizeId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid prize id"))
                    return@post
                }
                val added = addFavoritePrizeUseCase(principal.userId, prizeId)
                if (!added) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Prize was already in favorites"))
                    return@post
                }
                call.respond(HttpStatusCode.Created, mapOf("message" to "Prize added to favorites"))
            }

            delete("/users/me/prizes/{prizeId}") {
                val principal = call.principalOrError() ?: return@delete
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                if (prizeId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid prize id"))
                    return@delete
                }
                val removed = removeFavoritePrizeUseCase(principal.userId, prizeId)
                if (!removed) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Favorite prize not found"))
                    return@delete
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "Prize removed from favorites"))
            }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.principalOrError(): AuthPrincipal? {
    val jwtPrincipal = principal<JWTPrincipal>()
    if (jwtPrincipal == null) {
        respond(HttpStatusCode.Unauthorized, mapOf("message" to "Missing or invalid token"))
        return null
    }
    return AuthPrincipal(
        userId = jwtPrincipal.payload.getClaim("userId").asInt(),
        username = jwtPrincipal.payload.getClaim("username").asString()
    )
}

private data class AuthPrincipal(
    val userId: Int,
    val username: String
)

private fun ServerUser.toProfileResponse(): ProfileResponse {
    return ProfileResponse(
        id = id,
        username = username,
        role = role
    )
}

private fun toPrizeResponse(prize: ServerPrize): PrizeResponse {
    return PrizeResponse(
        id = prize.id,
        awardYear = prize.awardYear,
        category = prize.category,
        categoryTitle = prize.categoryTitle,
        detailLink = prize.detailLink,
        laureates = prize.laureates.map(::toLaureateResponse)
    )
}

private fun toLaureateResponse(laureate: ServerLaureate): LaureateResponse {
    return LaureateResponse(
        id = laureate.id,
        fullName = laureate.fullName,
        portion = laureate.portion,
        motivation = laureate.motivation,
        birthLocation = laureate.birthLocation,
        portraitUrl = laureate.portraitUrl
    )
}

data class PrizeResponse(
    val id: Int,
    val awardYear: String,
    val category: String,
    val categoryTitle: String,
    val detailLink: String,
    val laureates: List<LaureateResponse>
)

data class LaureateResponse(
    val id: String,
    val fullName: String,
    val portion: String,
    val motivation: String,
    val birthLocation: String,
    val portraitUrl: String?
)
