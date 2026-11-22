package com.knight.chesstrainer.openings

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface RepertoireGateway {
    suspend fun fetchRepertoires(): List<OpeningRepertoire>
}

class RestRepertoireGateway(
    private val client: HttpClient,
    private val baseUrl: String,
) : RepertoireGateway {
    override suspend fun fetchRepertoires(): List<OpeningRepertoire> =
        client.get("${baseUrl.trimEnd('/')}/api/repertoires").body()
}
