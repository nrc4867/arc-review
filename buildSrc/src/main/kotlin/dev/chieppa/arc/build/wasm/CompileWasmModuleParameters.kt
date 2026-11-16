package dev.chieppa.arc.build.wasm

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

/** What [CompileWasmModule] compiles: tree-sitter's runtime, the shim and one grammar as one WebAssembly module. */
interface CompileWasmModuleParameters : WorkParameters {
    val zig: RegularFileProperty
    val flags: ListProperty<String>
    val exports: ListProperty<String>
    val runtime: DirectoryProperty
    val grammar: DirectoryProperty
    val shim: RegularFileProperty
    val inlineHeader: RegularFileProperty
    val lexerChunkChars: Property<Int>
    val output: RegularFileProperty
    val scratch: DirectoryProperty
    val zigCache: DirectoryProperty
}
