import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.publish.plugin)
    alias(libs.plugins.gradle.versions)
}

allprojects {
    plugins.withType<JavaBasePlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(
                    JavaLanguageVersion.of(rootProject.libs.versions.javaVersion.get().toInt())
                )
            }
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = true
        }
    }
    apply(plugin = rootProject.libs.plugins.ktlint.gradle.get().pluginId)
    ktlint {
        version.set(rootProject.libs.versions.ktlint.asProvider())
        android.set(true)
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
