import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PrinceOfVersions"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("io.ktor:ktor-client-core:2.3.4")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:2.3.4")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.4")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    explicitApi()
}

android {
    namespace = "com.infinum.princeofversions"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

detekt {
    buildUponDefaultConfig = true
    source.setFrom(
        files(
            "src/androidMain/kotlin",
            "src/commonMain/kotlin",
            "src/iosMain/kotlin",
            "src/jvmMain/kotlin"
        )
    )
}
