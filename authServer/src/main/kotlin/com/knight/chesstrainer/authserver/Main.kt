package com.knight.chesstrainer.authserver

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.knight.chesstrainer.openings.OpeningCatalog
import com.knight.chesstrainer.openings.OpeningRepertoire
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val ISSUER = "chess-trainer-auth"
private const val AUDIENCE = "chess-trainer-app"
private const val PASSWORD_ITERATIONS = 310_000
private const val PASSWORD_KEY_BITS = 256
private const val SESSION_DAYS = 14L

fun main() {
    val secret = System.getenv("CHESS_AUTH_JWT_SECRET")?.takeIf { it.length >= 32 }
        ?: error("Set CHESS_AUTH_JWT_SECRET to a random secret of at least 32 characters.")
    val databasePath = System.getenv("CHESS_AUTH_DB_PATH") ?: "authServer/data/users.sqlite"
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val store = AuthStore(databasePath)
    store.seedRepertoires(OpeningCatalog.starterLines)
    embeddedServer(Netty, host = "0.0.0.0", port = port) { authApplication(store, secret) }.start(wait = true)
}

private fun Application.authApplication(store: AuthStore, secret: String) {
    val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).withIssuer(ISSUER).withAudience(AUDIENCE).build()

    install(ContentNegotiation) { json() }
    install(Authentication) {
        jwt("auth-jwt") {
            this.verifier(verifier)
            validate { credential ->
                val email = credential.payload.subject
                val tokenId = credential.payload.id
                if (email != null && tokenId != null && !store.isRevoked(tokenId)) JWTPrincipal(credential.payload) else null
            }
        }
    }

    routing {
        get("/health") { call.respond(HealthResponse("ok")) }
        get("/api/repertoires") { call.respond(store.repertoires()) }
        route("/api/auth") {
            post("/signup") {
                val request = call.receive<AuthRequest>()
                val email = request.email.trim().lowercase()
                val displayName = request.displayName?.trim().orEmpty()
                if (!EMAIL_REGEX.matches(email)) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Enter a valid email address.")); return@post
                }
                if (displayName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Enter your display name.")); return@post
                }
                if (request.password.length < 8) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Password must be at least 8 characters.")); return@post
                }
                val created = store.createUser(email, displayName, hashPassword(request.password))
                if (!created) {
                    call.respond(HttpStatusCode.Conflict, ApiError("An account with this email already exists.")); return@post
                }
                call.respond(issueSession(email, displayName, store, algorithm))
            }

            post("/signin") {
                val request = call.receive<AuthRequest>()
                val email = request.email.trim().lowercase()
                val account = store.findUser(email)
                if (account == null || !verifyPassword(request.password, account.passwordHash)) {
                    call.respond(HttpStatusCode.Unauthorized, ApiError("Email or password is incorrect.")); return@post
                }
                call.respond(issueSession(account.email, account.displayName, store, algorithm))
            }

            authenticate("auth-jwt") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()!!
                    call.respond(AuthUserResponse(principal.payload.subject!!, principal.payload.getClaim("name").asString() ?: "Chess player"))
                }
                post("/signout") {
                    val principal = call.principal<JWTPrincipal>()!!
                    principal.payload.id?.let(store::revoke)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun issueSession(email: String, displayName: String, store: AuthStore, algorithm: Algorithm): AuthResponse {
    val tokenId = UUID.randomUUID().toString()
    val token = JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withSubject(email)
        .withClaim("name", displayName)
        .withJWTId(tokenId)
        .withExpiresAt(java.util.Date(System.currentTimeMillis() + SESSION_DAYS * 24 * 60 * 60 * 1000))
        .sign(algorithm)
    store.recordToken(tokenId)
    return AuthResponse(token, AuthUserResponse(email, displayName))
}

private fun hashPassword(password: String): String {
    val salt = ByteArray(16).also(SecureRandom()::nextBytes)
    val spec = PBEKeySpec(password.toCharArray(), salt, PASSWORD_ITERATIONS, PASSWORD_KEY_BITS)
    val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    spec.clearPassword()
    val encoder = Base64.getEncoder()
    return "$PASSWORD_ITERATIONS:${encoder.encodeToString(salt)}:${encoder.encodeToString(hash)}"
}

private fun verifyPassword(password: String, encoded: String): Boolean = runCatching {
    val parts = encoded.split(':')
    require(parts.size == 3)
    val iterations = parts[0].toInt()
    val decoder = Base64.getDecoder()
    val salt = decoder.decode(parts[1])
    val expected = decoder.decode(parts[2])
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, expected.size * 8)
    val actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    spec.clearPassword()
    MessageDigest.isEqual(expected, actual)
}.getOrDefault(false)

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

private class AuthStore(path: String) {
    private val connection: Connection
    private val json = Json { ignoreUnknownKeys = true }

    init {
        File(path).absoluteFile.parentFile?.mkdirs()
        connection = DriverManager.getConnection("jdbc:sqlite:${File(path).absolutePath}")
        connection.createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (email TEXT PRIMARY KEY NOT NULL, display_name TEXT NOT NULL, password_hash TEXT NOT NULL)")
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS auth_tokens (token_id TEXT PRIMARY KEY NOT NULL, revoked INTEGER NOT NULL DEFAULT 0)")
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS opening_repertoires (id TEXT PRIMARY KEY NOT NULL, payload_json TEXT NOT NULL)")
        }
    }

    @Synchronized
    fun seedRepertoires(lines: List<OpeningRepertoire>) {
        connection.prepareStatement("INSERT OR REPLACE INTO opening_repertoires(id, payload_json) VALUES(?, ?)").use { query ->
            connection.autoCommit = false
            try {
                lines.forEach { line -> query.setString(1, line.id); query.setString(2, json.encodeToString(line)); query.addBatch() }
                query.executeBatch()
                connection.commit()
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    @Synchronized
    fun repertoires(): List<OpeningRepertoire> = connection.createStatement().use { statement ->
        statement.executeQuery("SELECT payload_json FROM opening_repertoires ORDER BY id").use { rows ->
            buildList { while (rows.next()) add(json.decodeFromString<OpeningRepertoire>(rows.getString(1))) }
        }
    }

    @Synchronized
    fun createUser(email: String, displayName: String, passwordHash: String): Boolean = try {
        connection.prepareStatement("INSERT INTO users(email, display_name, password_hash) VALUES(?, ?, ?)").use { query ->
            query.setString(1, email); query.setString(2, displayName); query.setString(3, passwordHash); query.executeUpdate()
        }
        true
    } catch (error: SQLException) {
        if (error.message.orEmpty().contains("unique", ignoreCase = true)) false else throw error
    }

    @Synchronized
    fun findUser(email: String): StoredUser? = connection.prepareStatement("SELECT email, display_name, password_hash FROM users WHERE email = ?").use { query ->
        query.setString(1, email)
        query.executeQuery().use { rows -> if (rows.next()) StoredUser(rows.getString(1), rows.getString(2), rows.getString(3)) else null }
    }

    @Synchronized
    fun recordToken(tokenId: String) { connection.prepareStatement("INSERT INTO auth_tokens(token_id, revoked) VALUES(?, 0)").use { it.setString(1, tokenId); it.executeUpdate() } }

    @Synchronized
    fun revoke(tokenId: String) { connection.prepareStatement("UPDATE auth_tokens SET revoked = 1 WHERE token_id = ?").use { it.setString(1, tokenId); it.executeUpdate() } }

    @Synchronized
    fun isRevoked(tokenId: String): Boolean = connection.prepareStatement("SELECT revoked FROM auth_tokens WHERE token_id = ?").use { query ->
        query.setString(1, tokenId)
        query.executeQuery().use { rows -> !rows.next() || rows.getInt(1) != 0 }
    }
}

private data class StoredUser(val email: String, val displayName: String, val passwordHash: String)

@Serializable private data class AuthRequest(val email: String, val password: String, val displayName: String? = null)
@Serializable private data class AuthResponse(val accessToken: String, val user: AuthUserResponse)
@Serializable private data class AuthUserResponse(val email: String, val displayName: String)
@Serializable private data class ApiError(val message: String)
@Serializable private data class HealthResponse(val status: String)
