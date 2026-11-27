import java.net.URI
import java.net.URISyntaxException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Load local configuration once.
// The root local.properties file must remain ignored by Git.
val localConfig = Properties().apply {
    val file = rootProject.file("local.properties")

    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

// Serverless backend endpoint.
// Required for building the Android application.
val pnrBackendUrl = localConfig
    .getProperty("pnr.api.baseUrl")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: error(
        "Set pnr.api.baseUrl in the ignored root local.properties file."
    )

// Validate the authority as well as the URL's textual form.
// Do not include the actual configured URL in validation errors.
val pnrBackendUri = try {
    URI(pnrBackendUrl).parseServerAuthority()
} catch (_: URISyntaxException) {
    null
}

require(
    pnrBackendUrl.startsWith("https://") &&
            pnrBackendUrl.endsWith("/") &&
            pnrBackendUri?.let { uri ->
                uri.scheme == "https" &&
                        uri.host?.isNotBlank() == true &&
                        uri.rawAuthority?.endsWith(":") == false &&
                        uri.rawUserInfo == null &&
                        uri.rawQuery == null &&
                        uri.rawFragment == null &&
                        uri.rawPath?.endsWith("/") == true &&
                        (uri.port == -1 || uri.port in 1..65535)
            } == true
) {
    "pnr.api.baseUrl must be a valid HTTPS base URL ending with /, " +
            "without credentials, query parameters or fragments."
}

// Optional GitHub repository URL.
// This value is loaded from local.properties,
// not hardcoded into version-controlled source code.
val githubRepoUrl = localConfig
    .getProperty("pnr.githubRepoUrl", "")
    .trim()
    .trimEnd('/')

// Escape the string for the generated Java BuildConfig source.
val escapedGithubUrl = githubRepoUrl
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

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

        buildConfigField(
            "String",
            "GITHUB_REPO_URL",
            "\"$escapedGithubUrl\""
        )
    }

    buildTypes {
        release {
            // Establish a reliable signed release baseline before
            // reintroducing R8 optimization.
            //
            // The previous package-scoped optimization configuration
            // produced an IllegalAccessError during Compose startup.
            //
            // Keep optimization disabled until an optimized release
            // has passed actual device runtime verification.
            optimization {
                enable = false
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
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    implementation(libs.androidx.core.splashscreen)

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