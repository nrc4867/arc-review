import dev.chieppa.arc.build.wasm.CompileWasm
import dev.chieppa.arc.build.wasm.CompileWasmClasses

plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

val pins = project(":syntax-engine").layout.projectDirectory.dir("tree-sitter")

// tree-sitter's runtime, the shim and each grammar as one WebAssembly module per grammar, built with the pinned zig.
val compileWasm = tasks.register<CompileWasm>("compileWasm") {
    group = "build"
    description = "Builds a tree-sitter WebAssembly module per grammar with zig"
    grammars = pins.file("grammars.tsv")
    shim = pins.file("arc_shim.c")
    inlineHeader = layout.projectDirectory.file("wasm/arc_inline.h")
    cacheDir = File(gradle.gradleUserHomeDir, "arc-natives")
    outputDir = layout.buildDirectory.dir("generated/wasm")
}

// Each module compiled to JVM classes, with its .meta beside them; the classes WasmGrammars names are compiled against.
val compileWasmClasses = tasks.register<CompileWasmClasses>("compileWasmClasses") {
    group = "build"
    description = "Compiles the tree-sitter WebAssembly modules to JVM classes with Chicory"
    wasmDir = compileWasm.flatMap { it.outputDir }
    packageName = "dev.chieppa.arc.syntax.wasm.generated"
    outputDir = layout.buildDirectory.dir("generated/wasm-classes")
}

val wasmClasses = files(compileWasmClasses.flatMap { it.outputDir }).builtBy(compileWasmClasses)

sourceSets.main {
    output.dir(mapOf("builtBy" to compileWasmClasses), compileWasmClasses.flatMap { it.outputDir })
}

// Each grammar's instance with Arc's query files compiled in (SnapshotWriter), so they are never compiled at run time.
val queries = project(":syntax").layout.projectDirectory.dir("src/main/resources/dev/chieppa/arc/syntax/queries")
val writeSnapshots = tasks.register<JavaExec>("writeSnapshots") {
    group = "build"
    description = "Writes each grammar's WebAssembly memory with Arc's queries compiled in"
    val out = layout.buildDirectory.dir("generated/wasm-snapshots")
    val folder = out.map { it.dir("dev/chieppa/arc/syntax/wasm/snapshots") }
    inputs.dir(queries).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(out)
    outputs.cacheIf { true }
    // Main's classes, not its whole output, since the jar holding these snapshots is built from that.
    classpath = files(kotlin.target.compilations.getByName("main").output.classesDirs, configurations.runtimeClasspath, wasmClasses)
    mainClass = "dev.chieppa.arc.syntax.wasm.SnapshotWriter"
    val queryFolder = queries.asFile.path
    argumentProviders += CommandLineArgumentProvider { listOf(queryFolder, folder.get().asFile.path) }
}

// The snapshots go into the jar, which other modules run with.
tasks.jar {
    from(writeSnapshots)
}

dependencies {
    api(project(":syntax-engine"))
    implementation(libs.chicory.runtime)
    compileOnly(wasmClasses)
    testImplementation(kotlin("test"))
    testRuntimeOnly(files(writeSnapshots))
}

tasks.test {
    useJUnitPlatform()
}
