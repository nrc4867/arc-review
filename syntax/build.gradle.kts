import dev.chieppa.arc.build.natives.CompileNatives
import dev.chieppa.arc.build.natives.NativeTarget

plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

// The tree-sitter libraries, built from the pinned sources with zig: the host's platform by default,
// -Pnatives=all for every platform, or -Pnatives=windows-x64,macos-aarch64 for a list.
val compileNatives = tasks.register<CompileNatives>("compileNatives") {
    group = "build"
    description = "Builds the tree-sitter core and grammar libraries with zig"
    grammars = layout.projectDirectory.file("native/grammars.tsv")
    shim = layout.projectDirectory.file("native/arc_shim.c")
    targets = providers.gradleProperty("natives").map { value -> NativeTarget.select(value).map { it.platform } }
        .orElse(NativeTarget.select(null).map { it.platform })
    // The resource folder NativeLibraries reads from.
    resourcePath = "dev/chieppa/arc/syntax/natives"
    cacheDir = File(gradle.gradleUserHomeDir, "arc-natives")
    outputDir = layout.buildDirectory.dir("generated/natives")
}

sourceSets.main {
    resources.srcDir(compileNatives.flatMap { it.outputDir })
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
