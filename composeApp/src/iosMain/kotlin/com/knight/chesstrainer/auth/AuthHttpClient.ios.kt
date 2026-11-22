package com.knight.chesstrainer.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createAuthHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}
