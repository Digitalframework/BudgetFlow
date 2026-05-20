plugins {
    // Trick: for the same plugin to be applied in all modules,
    // specify the version in the root build.gradle.kts file
    // and apply the plugin without version in the submodules
    kotlin("multiplatform") version "2.1.20" apply false
    kotlin("android") version "2.1.20" apply false
    kotlin("plugin.compose") version "2.1.20" apply false
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20" apply false
}
