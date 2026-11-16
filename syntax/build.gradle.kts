plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

// The syntax engine at run time: tree-sitter as WebAssembly in pure Java, or with -Psyntax.engine=native the native
// libraries over FFM, for comparison.
val nativeEngine = providers.gradleProperty("syntax.engine").orNull == "native"

// What facts depend on: this module's code and queries and the tree-sitter pins. FactsVersion reads the hash, so facts
// kept on disk are read again only by the same version.
val writeFactsVersion = tasks.register<dev.chieppa.arc.build.WriteContentHash>("writeFactsVersion") {
    sources.from(layout.projectDirectory.dir("src/main"), project(":syntax-engine").layout.projectDirectory.dir("tree-sitter"))
    hashFile = "dev/chieppa/arc/syntax/facts/facts-version.txt"
    outputDir = layout.buildDirectory.dir("generated/facts-version")
}

sourceSets.main {
    resources.srcDir(writeFactsVersion.flatMap { it.outputDir })
}

dependencies {
    api(project(":syntax-engine"))
    runtimeOnly(project(if (nativeEngine) ":syntax-native" else ":syntax-wasm"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
