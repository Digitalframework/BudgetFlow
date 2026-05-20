plugins {
    kotlin("multiplatform") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    kotlin("plugin.compose") version "2.1.20"
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {

        val jsMain by getting {

            kotlin.srcDir("src/jsMain/kotlin")  // ← add this
            resources.srcDir("src/jsMain/resources")
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(project(":shared"))
                // BOM manages all wrapper versions — only declare it once
                implementation(project.dependencies.platform(
                    "org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.5.3"
                ))

                // No versions needed — BOM handles them
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-web")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.9.0")

                // Compose Multiplatform runtime for JS
                implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
                implementation("org.jetbrains.compose.ui:ui:1.7.3")
                implementation("org.jetbrains.compose.foundation:foundation:1.7.3")
                implementation("org.jetbrains.compose.material:material:1.7.3")

                implementation(npm("antd", "5.24.7"))
                implementation(npm("@ant-design/icons", "5.3.0"))
                implementation(npm("pdfjs-dist", "4.10.38"))

            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}