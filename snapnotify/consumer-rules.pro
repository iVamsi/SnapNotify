# SnapNotify ProGuard rules
-keep class com.snapnotify.** { *; }
-keepclassmembers class com.snapnotify.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }