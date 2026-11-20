pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // PREFER_PROJECT is required for Kotlin Multiplatform WasmJs root setup tasks
    // (:kotlinWasmNodeJsSetup / :kotlinWasmYarnSetup), which register temporary Ivy
    // distribution repositories on the root project. Subprojects do not declare
    // project repositories and resolve from the settings repositories below.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        if (providers.gradleProperty("useMavenLocal").orNull.toBoolean()) {
            mavenLocal()
        }
        mavenCentral()
    }
}

rootProject.name = "SnapNotify"
include(":snapnotify")
include(":SnapNotifyDemo")
include(":samples:ios-native")
include(":samples:kmm-app")
include(":samples:cmp-app")
include(":samples:web-app")
include(":samples:desktop-mac")
include(":samples:desktop-windows")