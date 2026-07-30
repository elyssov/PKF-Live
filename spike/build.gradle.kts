import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

// GATE SPIKE: one game (Pong) on a Compose Multiplatform canvas in the
// browser via Kotlin/Wasm. If this stands, the arcade hall gets built on
// the same copy-and-patch pipeline as the desktop port. If it falls, we
// name the wall a wall BEFORE painting the cash desk.
plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "arcade.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
            }
        }
    }
}
