package com.knight.chesstrainer.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

class RestAuthGateway(
    private val client: HttpClient,
    baseUrl: String,
) : AuthGateway {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun authenticate(credentials: AuthCredentials): AuthUser {
        val path = if (credentials.createAccount) "/api/auth/signup" else "/api/auth/signin"
        val response = client.post(baseUrl + path) {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(credentials.email, credentials.password, credentials.displayName))
        }
        if (!response.status.isSuccess()) {
            val message = runCatching { response.body<AuthError>().message }.getOrNull()
            error(message ?: "Authentication failed (${response.status.value}).")
        }
        val result = response.body<AuthResponse>()
        return AuthUser(result.user.email, result.user.displayName, result.accessToken)
    }

    override suspend fun signOut(accessToken: String) {
        val response = client.post("$baseUrl/api/auth/signout") { bearerAuth(accessToken) }
        if (!response.status.isSuccess()) error("Sign out failed (${response.status.value}).")
    }
}

@Serializable
private data class AuthRequest(val email: String, val password: String, val displayName: String? = null)

@Serializable
private data class AuthError(val message: String)

@Serializable
private data class AuthResponse(val accessToken: String, val user: AuthUserResponse)

@Serializable
private data class AuthUserResponse(val email: String, val displayName: String)
