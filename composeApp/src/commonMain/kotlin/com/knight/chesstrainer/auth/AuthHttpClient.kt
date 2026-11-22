package com.knight.chesstrainer.auth

import io.ktor.client.HttpClient

expect fun createAuthHttpClient(): HttpClient
