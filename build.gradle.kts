plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

allprojects {
    group = "dev.chieppa.arc"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    val checkTestSources = tasks.register<dev.chieppa.arc.build.CheckTestSources>("checkTestSources") {
        group = "verification"
        sources.from(fileTree("src") { include("test/**/*.kt", "*Test/**/*.kt") })
    }
    tasks.matching { it.name == "check" }.configureEach { dependsOn(checkTestSources) }
}
