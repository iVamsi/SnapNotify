import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

android {
    namespace = "com.vamsi.snapnotify"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    buildFeatures {
        compose = true
    }
    
}

dependencies {
    // Core Compose dependencies (essential for library functionality)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Essential AndroidX dependencies (keep as implementation to avoid conflicts)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Optional dependencies
    compileOnly(libs.androidx.ui.tooling.preview)
    
    // Optional DI framework support (make Hilt completely optional)
    compileOnly(libs.hilt.android)
    compileOnly(libs.hilt.navigation.compose)
    compileOnly(libs.hilt.compiler)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    
    coordinates("io.github.ivamsi", "snapnotify", "1.0.4")

    pom {
        name.set("SnapNotify")
        description.set("A developer-friendly Snackbar library for Jetpack Compose that simplifies displaying snackbars with zero-ceremony setup")
        url.set("https://github.com/ivamsi/snapnotify")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
            connection.set("scm:git:git://github.com/ivamsi/snapnotify.git")
            developerConnection.set("scm:git:ssh://git@github.com/ivamsi/snapnotify.git")
        }
    }

    configure(AndroidSingleVariantLibrary(
        variant = "release",
        sourcesJar = true,
        publishJavadocJar = true
    ))
}


