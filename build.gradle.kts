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

// Force io.netty to 4.1.133.Final across all configurations to address:
// CVE-2026-42583 (GHSA-mj4r-2hfc-f8p6), CVE-2026-42584 (GHSA-57rv-r2g8-2cj3),
// CVE-2026-42585 (GHSA-38f8-5428-x5cv), CVE-2026-42580 (GHSA-m4cv-j2px-7723),
// CVE-2026-42581 (GHSA-xxqh-mfjm-7mv9), CVE-2026-41417 (GHSA-v8h7-rr48-vmmv),
// CVE-2026-42587 (GHSA-f6hv-jmp6-3vwv), CVE-2026-42578 (GHSA-45q3-82m4-75jr)
allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.netty") {
                useVersion("4.1.133.Final")
                because("Bumps all io.netty modules to 4.1.133.Final to fix multiple high/medium/low CVEs (GHSA-f6hv-jmp6-3vwv, GHSA-57rv-r2g8-2cj3, GHSA-mj4r-2hfc-f8p6, GHSA-38f8-5428-x5cv, GHSA-xxqh-mfjm-7mv9, GHSA-m4cv-j2px-7723, GHSA-v8h7-rr48-vmmv, GHSA-45q3-82m4-75jr)")
            }
        }
    }
}