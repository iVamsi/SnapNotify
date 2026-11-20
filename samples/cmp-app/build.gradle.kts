import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.vamsi.snapnotify.samples.cmp"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64 {
        binaries {
            executable {
                entryPoint = "com.vamsi.snapnotify.samples.cmp.main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                    "-linker-option", "-framework", "-linker-option", "QuartzCore",
                    "-linker-option", "-framework", "-linker-option", "UIKit",
                    "-Xbinary=bundleId=com.vamsi.snapnotify.samples.cmp"
                )
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
            implementation(project(":snapnotify"))
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.ui.graphics)
            implementation(libs.jetbrains.compose.material3)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.vamsi.snapnotify.samples.cmp.MainKt"
    }
}

tasks.matching {
    it.name in setOf(
        "iosSimulatorArm64Test",
        "checkComposeUiTestConfigurationForWasmJs"
    )
}.configureEach {
    enabled = false
}

tasks.register("packageIosSimulatorApp") {
    group = "application"
    description = "Packages the CMP shared UI iOS Simulator executable into SnapNotifyCmpApp.app."
    dependsOn("linkDebugExecutableIosSimulatorArm64")
    val executableFile = layout.buildDirectory.file("bin/iosSimulatorArm64/debugExecutable/cmp-app.kexe")
    val infoPlistFile = layout.projectDirectory.file("iosApp/Info.plist")
    val appBundleDir = layout.buildDirectory.dir("iosApp/SnapNotifyCmpApp.app")
    inputs.file(executableFile)
    inputs.file(infoPlistFile)
    outputs.dir(appBundleDir)
    doLast {
        val bundleDir = appBundleDir.get().asFile
        bundleDir.mkdirs()
        val targetExec = bundleDir.resolve("SnapNotifyCmpApp")
        executableFile.get().asFile.copyTo(targetExec, overwrite = true)
        targetExec.setExecutable(true)
        infoPlistFile.asFile.copyTo(bundleDir.resolve("Info.plist"), overwrite = true)
    }
}
