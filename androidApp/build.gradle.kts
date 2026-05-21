plugins {
    kotlin("multiplatform")
    id("com.android.application")
    kotlin("plugin.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val androidMain by getting {
            // Point the Kotlin Multiplatform plugin to your newly renamed directory structure
            kotlin.srcDirs("src/androidMain/kotlin")
            resources.srcDirs("src/androidMain/res")

            dependencies {
                implementation(project(":shared"))
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.compose.ui:ui:1.5.4")
                implementation("androidx.compose.ui:ui-graphics:1.5.4")
                implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
                implementation("androidx.compose.material3:material3:1.1.2")
                implementation("androidx.compose.material:material-icons-extended:1.5.4")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
            }
        }
    }
}

android {
    namespace = "com.banking.app"
    compileSdk = 34

    // Crucial Adjustment: Map Android's core build system to look into androidMain
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            java.srcDirs("src/androidMain/kotlin")
        }
    }

    defaultConfig {
        applicationId = "com.banking.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts.add("META-INF/native-image/org.mongodb/bson/native-image.properties")        }
    }
}