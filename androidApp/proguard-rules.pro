# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin data classes
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }

# Keep MongoDB classes
-keep class org.mongodb.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }