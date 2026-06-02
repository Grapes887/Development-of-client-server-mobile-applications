plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "com.example.module6.server5"
version = "1.0"

application {
    mainClass = "com.example.module6.server5.ServerMainKt"
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.serialization.gson.jvm)
    implementation(libs.hikari)
    implementation(libs.postgresql)
    implementation(libs.slf4j.simple)
}
