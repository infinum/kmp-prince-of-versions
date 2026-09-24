import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.detekt)
    alias(libs.plugins.skie)
    alias(libs.plugins.gradle.maven.publish)
}

kotlin {
    android {
        namespace = "com.infinum.princeofversions"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        withHostTest { }
    }

    val xcFramework = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PrinceOfVersions"
            isStatic = true

            binaryOptions["bundleId"] = "com.infinum.princeofversions"

            xcFramework.add(this)
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
        getByName("androidHostTest").dependencies {
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

val groupId = providers.gradleProperty("groupId").get()

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "kmp-prince-of-versions",
        version = libs.versions.prince.of.versions.get()
    )
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
