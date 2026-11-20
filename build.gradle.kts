// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.legacy.kapt) apply false
}

// Regression guard: ensure the consumer demo assembles with the latest library metadata.
tasks.register("verifyDemoConsumerRelease") {
    group = "verification"
    description = "Assembles SnapNotifyDemo release to ensure consumer builds stay healthy."
    dependsOn(":SnapNotifyDemo:assembleRelease")
}

tasks.named("check") {
    dependsOn("verifyDemoConsumerRelease")
    dependsOn(subprojects.filter { it.childProjects.isEmpty() }.map { "${it.path}:check" })
}