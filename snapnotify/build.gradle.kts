import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    android {
        namespace = "com.vamsi.snapnotify"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("consumer-rules.pro")
            }
        }

        withHostTest {}
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SnapNotify"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.vamsi.snapnotify")
        }
        iosTarget.binaries.configureEach {
            if (name.contains("test", ignoreCase = true)) {
                freeCompilerArgs += listOf("-Xbinary=bundleId=com.vamsi.snapnotify.test")
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core Compose Multiplatform dependencies (exposed because public APIs reference them)
            api(libs.jetbrains.compose.runtime)
            api(libs.jetbrains.compose.foundation)
            api(libs.jetbrains.compose.ui)
            api(libs.jetbrains.compose.ui.graphics)
            api(libs.jetbrains.compose.material3)

            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.ui.test.junit4)
            implementation(libs.androidx.ui.test.manifest)
        }
    }
}

tasks.matching {
    it.name == "iosSimulatorArm64Test"
}.configureEach {
    enabled = providers.gradleProperty("enableIosSimulatorTests").map { it.toBoolean() }.getOrElse(false)
}

tasks.matching {
    it.name in setOf(
        "checkComposeUiTestConfigurationForWasmJs",
        "copyAndroidDeviceTestComposeResourcesToAndroidAssets"
    )
}.configureEach {
    enabled = false
}

tasks.named("check") {
    dependsOn(tasks.matching {
        it.name in setOf(
            "compileKotlinIosArm64",
            "compileKotlinIosSimulatorArm64",
            "compileTestKotlinIosSimulatorArm64",
            "compileKotlinWasmJs",
            "compileTestKotlinWasmJs"
        )
    })
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates("io.github.ivamsi", "snapnotify", "2.0.0")

    pom {
        name.set("SnapNotify")
        description.set("A developer-friendly Snackbar library for Compose Multiplatform and Jetpack Compose that simplifies displaying snackbars with zero-ceremony setup")
        url.set("https://github.com/ivamsi/snapnotify")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("ivamsi")
                name.set("Vamsi Vaddavalli")
                url.set("https://github.com/ivamsi")
            }
        }

        scm {
            url.set("https://github.com/ivamsi/snapnotify")
            connection.set("scm:git:https://github.com/ivamsi/snapnotify.git")
            developerConnection.set("scm:git:ssh://git@github.com/ivamsi/snapnotify.git")
        }
    }
}
