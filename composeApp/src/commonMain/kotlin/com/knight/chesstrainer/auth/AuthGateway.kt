package com.knight.chesstrainer.auth

data class AuthCredentials(
    val email: String,
    val password: String,
    val displayName: String? = null,
    val createAccount: Boolean,
)

data class AuthUser(val email: String, val displayName: String, val accessToken: String)

/** Platform hosts supply a real network-backed implementation; no local fake auth is used. */
interface AuthGateway {
    suspend fun authenticate(credentials: AuthCredentials): AuthUser
    suspend fun signOut(accessToken: String)
}
