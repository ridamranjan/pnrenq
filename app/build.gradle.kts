import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Read the backend address from an ignored local file.

val localConfig = Properties().apply {
    val file = rootProject.file("local.properties")

    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}


val pnrBackendUrl = localConfig
    .getProperty("pnr.api.baseUrl")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: error(
        "Set pnr.api.baseUrl in the ignored root local.properties file."
    )

require(
    pnrBackendUrl.startsWith("https://") &&
            pnrBackendUrl.endsWith("/") &&
            !pnrBackendUrl.contains("\"") &&
            !pnrBackendUrl.contains("\\") &&
            !pnrBackendUrl.contains("?") &&
            !pnrBackendUrl.contains("#")
) {
    "pnr.api.baseUrl must be a valid HTTPS base URL ending with /."
}

android {
    namespace = "com.first.pnrenq"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.first.pnrenq"
        minSdk = 26
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "PNR_API_BASE_URL",
            "\"$pnrBackendUrl\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    implementation("androidx.core:core-splashscreen:1.2.0")

    testImplementation(libs.junit)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )
    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
