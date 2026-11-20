import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
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
        mainClass = "com.vamsi.snapnotify.samples.mac.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "SnapNotifyMacDemo"
            packageVersion = "2.0.0"
            macOS {
                bundleID = "com.vamsi.snapnotify.samples.mac"
            }
        }
    }
}

tasks.register<JavaExec>("renderSnapshot") {
    group = "verification"
    description = "Renders an offscreen PNG snapshot of the macOS Desktop sample with an active snackbar."
    val jvmCompilation = kotlin.jvm().compilations.getByName("main")
    dependsOn(jvmCompilation.compileTaskProvider)
    classpath(jvmCompilation.output.allOutputs, jvmCompilation.runtimeDependencyFiles)
    mainClass.set("com.vamsi.snapnotify.samples.mac.MainKt")
    jvmArgs("-Djava.awt.headless=true")
    val outFile = layout.buildDirectory.file("snapshots/mac_desktop_demo.png")
    outputs.file(outFile)
    args("--snapshot", outFile.get().asFile.absolutePath)
}
