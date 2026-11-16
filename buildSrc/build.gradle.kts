plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // Compiles the tree-sitter WebAssembly modules to JVM classes at build time (:syntax-wasm:compileWasmClasses).
    implementation(libs.chicory.build.time.compiler)
}
