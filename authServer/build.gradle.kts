plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
    application
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.java.jwt)
    implementation(libs.sqlite.jdbc)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(17)
    sourceSets.main { kotlin.srcDir("../sharedOpenings/src/main/kotlin") }
}

application { mainClass.set("com.knight.chesstrainer.authserver.MainKt") }
