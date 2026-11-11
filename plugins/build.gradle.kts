import dev.chieppa.arc.build.plugins.BundlePluginHost

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

val sdk = rootProject.layout.projectDirectory.dir("plugin-sdk")

// The plugin host: plugin-sdk's host bootstrap with React, react-reconciler and the SDK in one file, bundled with the
// esbuild pinned in tools.properties; the npm packages and esbuild are kept in ~/.gradle/arc-plugin-tools.
val bundlePluginHost = tasks.register<BundlePluginHost>("bundlePluginHost") {
    group = "build"
    description = "Bundles the plugin host with esbuild"
    pins = layout.projectDirectory.file("src/main/resources/dev/chieppa/arc/plugins/runtime/tools.properties")
    sources = sdk.dir("src")
    entry = "host/main.ts"
    packages = listOf("react", "react-reconciler", "scheduler")
    // The resource PluginHostScript reads.
    resourcePath = "dev/chieppa/arc/plugins/host/host.js"
    cacheDir = File(gradle.gradleUserHomeDir, "arc-plugin-tools")
    outputDir = layout.buildDirectory.dir("generated/plugin-host")
}

sourceSets.main {
    resources.srcDir(bundlePluginHost.flatMap { it.outputDir })
}

// plugin-sdk/src/generated/arc.d.ts from the Kotlin definitions; PluginTypesTest fails while it is stale.
tasks.register<JavaExec>("generatePluginTypes") {
    group = "arc"
    description = "Writes the plugin API's TypeScript types"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "dev.chieppa.arc.plugins.api.GeneratePluginTypesKt"
    args(sdk.file("src/generated/arc.d.ts").asFile.path)
}

tasks.test {
    useJUnitPlatform { excludeTags("node") }
    systemProperty("arc.repoRoot", rootProject.layout.projectDirectory.asFile.path)
    inputs.dir(sdk.dir("src/generated"))
}

// The tests that run the bundled host in a real Node: -Parc.plugins.node=<node> names it. Not part of build.
tasks.register<Test>("nodeTest") {
    group = "verification"
    description = "Runs the plugin host in Node"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags("node") }
    systemProperty("arc.plugins.node", providers.gradleProperty("arc.plugins.node").getOrElse("node"))
    systemProperty("arc.repoRoot", rootProject.layout.projectDirectory.asFile.path)
}
