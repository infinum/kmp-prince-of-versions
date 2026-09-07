import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    implementation(projects.sampleShared)
    implementation(projects.princeofversions)

    implementation(compose.desktop.currentOs)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.json)
    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "com.infinum.princeofversions.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.infinum.princeofversions.sample"
            packageVersion = "1.0.0" // JVM desktop apps must start from 1.0.0
        }
    }
}
