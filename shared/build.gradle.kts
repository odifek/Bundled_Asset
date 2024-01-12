plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation(libs.squareup.okio)
            implementation(libs.coroutines)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.coroutines.swing)
            }
        }
    }
}

android {
    namespace = "com.techbeloved.bundledasset"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }

    kotlin {
        jvmToolchain(17)
    }

    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources/res")
        assets.srcDirs("src/commonMain/resources/assets")
    }
}
