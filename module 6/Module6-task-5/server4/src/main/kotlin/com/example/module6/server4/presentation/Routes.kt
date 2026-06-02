package com.example.module6.server4.presentation

import com.example.module6.server4.domain.model.Laureate
import com.example.module6.server4.domain.model.NobelPrize
import com.example.module6.server4.domain.usecase.AuthenticateUserUseCase
import com.example.module6.server4.domain.usecase.GetLaureatesByPrizeUseCase
import com.example.module6.server4.domain.usecase.GetPrizeByYearCategoryUseCase
import com.example.module6.server4.domain.usecase.GetPrizesUseCase
import com.example.module6.server4.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
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

fun Application.configureRouting(
    authenticateUserUseCase: AuthenticateUserUseCase,
    getPrizesUseCase: GetPrizesUseCase,
    getPrizeByYearCategoryUseCase: GetPrizeByYearCategoryUseCase,
    getLaureatesByPrizeUseCase: GetLaureatesByPrizeUseCase
) {
    routing {
        route("/auth") {
            post("/login") {
                val request = call.receive<LoginRequest>()
                if (!authenticateUserUseCase(request.username, request.password)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid credentials"))
                    return@post
                }

                call.respond(
                    LoginResponse(
                        token = JwtConfig.generateToken(request.username),
                        expiresInMinutes = 30
                    )
                )
            }
        }

        authenticate("auth-jwt") {
            get("/prizes") {
                call.respond(getPrizesUseCase().map(::toPrizeResponse))
            }

            get("/prizes/{year}/{category}") {
                val year = call.parameters["year"]
                val category = call.parameters["category"]
                if (year == null || category == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing year or category"))
                    return@get
                }

                val prize = getPrizeByYearCategoryUseCase(year, category)
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
        }
    }
}

private fun toPrizeResponse(prize: NobelPrize): PrizeResponse {
    return PrizeResponse(
        awardYear = prize.awardYear,
        category = prize.category,
        categoryTitle = prize.categoryTitle,
        detailLink = prize.detailLink,
        laureates = prize.laureates.map(::toLaureateResponse)
    )
}

private fun toLaureateResponse(laureate: Laureate): LaureateResponse {
    return LaureateResponse(
        id = laureate.id,
        fullName = laureate.fullName,
        portion = laureate.portion,
        motivation = laureate.motivation,
        portraitUrl = laureate.portraitUrl
    )
}

data class PrizeResponse(
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
    val portraitUrl: String?
)
