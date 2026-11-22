plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.knight.chesstrainer.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures { buildConfig = true }
    packaging {
        jniLibs.useLegacyPackaging = true
    }

    defaultConfig {
        applicationId = "com.knight.chesstrainer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        val authBaseUrl = providers.gradleProperty("chessAuthBaseUrl")
            .orElse(providers.environmentVariable("CHESS_AUTH_BASE_URL"))
            .getOrElse("http://127.0.0.1:8080")
        buildConfigField("String", "AUTH_BASE_URL", "\"$authBaseUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.12.1")
}
