plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// Force io.opentelemetry:opentelemetry-api to 1.62.0 across all configurations to address:
// CVE-2026-45292 (GHSA-rcgg-9c38-7xpx) — Unbounded Memory Allocation in W3C Baggage Propagation
allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.opentelemetry" && requested.name == "opentelemetry-api") {
                useVersion("1.62.0")
                because("Fixes CVE-2026-45292 (GHSA-rcgg-9c38-7xpx): unbounded memory allocation in W3C Baggage Propagation")
            }
        }
    }
}