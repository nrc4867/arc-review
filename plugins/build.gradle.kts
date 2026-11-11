import dev.chieppa.arc.build.plugins.BundlePluginHost

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    implementation(project(":git"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

val sdk = rootProject.layout.projectDirectory.dir("plugin-sdk")

// The plugin host: plugin-sdk's host with React, react-reconciler and the SDK in one file, bundled with the esbuild
// pinned in plugin-sdk/host-tools.properties, and TypeScript's compiler beside it; the npm packages and esbuild are
// kept in ~/.gradle/arc-plugin-tools.
val bundlePluginHost = tasks.register<BundlePluginHost>("bundlePluginHost") {
    group = "build"
    description = "Bundles the plugin host with esbuild"
    pins = sdk.file("host-tools.properties")
    sources = sdk.dir("src")
    entry = "host/main.ts"
    packages = listOf("react", "react-reconciler", "scheduler", "typescript")
    copies = mapOf("typescript/lib/typescript.js" to "typescript.js")
    // What the type check reads: TypeScript's own declarations, the SDK's types, and React's when a plugin has none.
    packageFiles = mapOf("typescript/lib/lib.*.d.ts" to ".")
    sourceFiles = mapOf(
        "sdk/*.ts" to "types/sdk",
        "generated/*.d.ts" to "types/generated",
        "check/shims/*.d.ts" to "types/shims",
        // What a starter plugin carries beside its types.
        "generated/*.md" to "docs",
    )
    // The resource PluginHostScript reads.
    resourcePath = "dev/chieppa/arc/plugins/host/host.js"
    cacheDir = File(gradle.gradleUserHomeDir, "arc-plugin-tools")
    outputDir = layout.buildDirectory.dir("generated/plugin-host")
}

sourceSets.main {
    resources.srcDir(bundlePluginHost.flatMap { it.outputDir })
}

// plugin-sdk/src/generated/arc.d.ts and reference.md from the Kotlin definitions; PluginTypesTest fails while either is stale.
tasks.register<JavaExec>("generatePluginTypes") {
    group = "arc"
    description = "Writes the plugin API's TypeScript types and Markdown reference"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "dev.chieppa.arc.plugins.api.GeneratePluginTypesKt"
    args(sdk.file("src/generated/arc.d.ts").asFile.path, sdk.file("src/generated/reference.md").asFile.path)
}

tasks.test {
    useJUnitPlatform { excludeTags("node", "keychain") }
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

// The test that writes, reads and deletes one made-up secret in this system's real keychain. Not part of build.
tasks.register<Test>("keychainTest") {
    group = "verification"
    description = "Runs the plugin secrets against the system keychain"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags("keychain") }
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
