plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "net.simno.kortholt"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = libs.versions.ndk.get()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++14")
                arguments("-DANDROID_STL=c++_shared")
                abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}

dependencies {
    api(libs.coroutines.core)
    api(libs.androidx.annotation)
    implementation(libs.androidx.core)
    implementation(libs.relinker)
    implementation(libs.zip4j)
}

val siteUrl = "https://github.com/simonnorberg/kortholt"
val gitUrl = "https://github.com/simonnorberg/kortholt.git"

version = "3.0.0"
group = "net.simno.kortholt"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "net.simno.kortholt"
                artifactId = "kortholt"
                version = "3.0.0"
                pom {
                    name.set("kortholt")
                    url.set(siteUrl)
                    description.set("Pure Data for Android with libpd and Oboe.")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        connection.set(gitUrl)
                        developerConnection.set(gitUrl)
                        url.set(siteUrl)
                    }
                    developers {
                        developer {
                            id.set("simonnorberg")
                            name.set("Simon Norberg")
                        }
                    }
                }
            }
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

tasks.register<Javadoc>("javadoc") {
    exclude("**/*.kt")
    source = android.sourceSets["main"].java.getSourceFiles()
    classpath += files(android.bootClasspath.joinToString(File.pathSeparator))
}

tasks.register<Jar>("javadocJar") {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"].outputs.files)
}

artifacts {
    archives(tasks["javadocJar"])
    archives(tasks["sourcesJar"])
}

signing {
    val signingKey = rootProject.properties["SIGNING_KEY"]?.toString().orEmpty()
    val signingPassword = rootProject.properties["SIGNING_PASSWORD"]?.toString().orEmpty()
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
