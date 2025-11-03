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
        androidMain.dependencies {
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.core.ktx)
        }
        jvmMain.dependencies {
            implementation(libs.json)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.mockwebserver)
            implementation(libs.json)
        }
        jvmTest.dependencies {
            implementation(libs.mockwebserver)
            implementation(libs.json)
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
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

dependencies {
    detektPlugins(rootProject.libs.detekt.formatting)
}

detekt {
    config.setFrom(files("${rootProject.rootDir}/config/detekt.yml"))
    source.setFrom(
        files(
            "src/androidMain/kotlin",
            "src/commonMain/kotlin",
            "src/iosMain/kotlin",
            "src/jvmMain/kotlin"
        )
    )
}
