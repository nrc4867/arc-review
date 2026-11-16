plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

// The syntax engine at run time: tree-sitter as WebAssembly in pure Java, or with -Psyntax.engine=native the native
// libraries over FFM, for comparison.
val nativeEngine = providers.gradleProperty("syntax.engine").orNull == "native"

dependencies {
    api(project(":syntax-engine"))
    runtimeOnly(project(if (nativeEngine) ":syntax-native" else ":syntax-wasm"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
