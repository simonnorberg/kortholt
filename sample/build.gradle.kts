plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.cachefix)
}

android {
    namespace = "net.simno.kortholt.sample"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "net.simno.kortholt"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
    }
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.media)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material)
    lintChecks(libs.compose.lint)
    ktlintRuleset(libs.ktlint.compose)
}
