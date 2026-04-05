// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.language.base.plugins.LifecycleBasePlugin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.legacy.kapt) apply false
}

// Regression guard: ensure the consumer demo assembles with the latest library metadata.
tasks.register("verifyDemoConsumerRelease") {
    group = "verification"
    description = "Assembles SnapNotifyDemo release to ensure consumer builds stay healthy."
    dependsOn(":SnapNotifyDemo:assembleRelease")
}

val rootCheck = tasks.register("check") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs aggregated verification tasks for the root project."
}

rootCheck.configure {
    dependsOn("verifyDemoConsumerRelease")
    dependsOn(subprojects.map { "${it.path}:check" })
}