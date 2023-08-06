import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.publishPlugin)
    alias(libs.plugins.dependencyUpdates)
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
            allWarningsAsErrors = true
        }
    }
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/cpp/**/*.kt")
            ktlint(libs.versions.ktlint.get()).editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "android_studio",
                    "max_line_length" to 120
                )
            )
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(project.properties["SONATYPE_STAGING_PROFILE_ID"]?.toString().orEmpty())
            username.set(project.properties["OSSRH_USERNAME"]?.toString().orEmpty())
            password.set(project.properties["OSSRH_PASSWORD"]?.toString().orEmpty())
        }
    }
}
