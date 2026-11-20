import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.vamsi.snapnotify.samples.kmm"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SnapNotifyKmmShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":snapnotify"))
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.matching { it.name == "iosSimulatorArm64Test" }.configureEach {
    enabled = false
}

tasks.register<Exec>("buildIosSimulatorApp") {
    group = "application"
    description = "Compiles the KMM iOS SwiftUI app against SnapNotifyKmmShared.framework for the iOS Simulator."
    dependsOn("linkDebugFrameworkIosSimulatorArm64")

    val frameworkDir = layout.buildDirectory.dir("bin/iosSimulatorArm64/debugFramework")
    val swiftSource = layout.projectDirectory.file("iosApp/SnapNotifyKmmApp.swift")
    val infoPlist = layout.projectDirectory.file("iosApp/Info.plist")
    val appDir = layout.buildDirectory.dir("iosApp/SnapNotifyKmmApp.app")

    inputs.dir(frameworkDir)
    inputs.file(swiftSource)
    inputs.file(infoPlist)
    outputs.dir(appDir)

    doFirst {
        val bundle = appDir.get().asFile
        bundle.mkdirs()
        infoPlist.asFile.copyTo(bundle.resolve("Info.plist"), overwrite = true)
    }

    commandLine(
        "xcrun",
        "--sdk", "iphonesimulator",
        "swiftc",
        "-target", "arm64-apple-ios18.0-simulator",
        "-parse-as-library",
        swiftSource.asFile.absolutePath,
        "-F", frameworkDir.get().asFile.absolutePath,
        "-framework", "SnapNotifyKmmShared",
        "-framework", "SwiftUI",
        "-framework", "UIKit",
        "-framework", "Foundation",
        "-framework", "CoreGraphics",
        "-framework", "CoreText",
        "-framework", "QuartzCore",
        "-framework", "Metal",
        "-framework", "Accelerate",
        "-lc++",
        "-lsqlite3",
        "-o", appDir.get().asFile.resolve("SnapNotifyKmmApp").absolutePath
    )
}
