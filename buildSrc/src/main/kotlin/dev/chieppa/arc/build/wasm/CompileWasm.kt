package dev.chieppa.arc.build.wasm

import dev.chieppa.arc.build.natives.GrammarCheckouts
import dev.chieppa.arc.build.natives.GrammarPins
import dev.chieppa.arc.build.natives.ZigInstaller
import dev.chieppa.arc.build.natives.ZigRelease
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Builds one WebAssembly module per grammar in [grammars] (`<name>.wasm` in [outputDir]), each holding tree-sitter's
 * runtime (the `core` pin), the shim and the grammar. Zig and the sources are fetched into [cacheDir] only when the
 * modules are actually compiled, so a build-cache hit needs neither. The modules compile in parallel.
 */
@CacheableTask
abstract class CompileWasm : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val grammars: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val shim: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inlineHeader: RegularFileProperty

    @get:Input
    abstract val zigVersion: Property<String>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val exports: ListProperty<String>

    @get:Input
    abstract val lexerChunkChars: Property<Int>

    /** The shared folder zig, the sources and zig's own cache are kept in, across builds and checkouts. */
    @get:Internal
    abstract val cacheDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val workers: WorkerExecutor

    @get:Inject
    abstract val exec: ExecOperations

    @get:Inject
    abstract val files: FileSystemOperations

    @get:Inject
    abstract val archives: ArchiveOperations

    init {
        zigVersion.convention(ZigRelease.VERSION)
        flags.convention(WasmFlags.COMMON)
        exports.convention(WasmFlags.EXPORTS)
        lexerChunkChars.convention(WasmFlags.LEXER_CHUNK_CHARS)
    }

    @TaskAction
    fun compile() {
        val out = outputDir.get().asFile
        files.delete { delete(out) }
        out.mkdirs()
        val cache = cacheDir.get().asFile
        val zig = ZigInstaller(exec, files, archives).install(cache.resolve(ZIG_FOLDER))
        val pins = GrammarPins.read(grammars.get().asFile)
        val sources = GrammarCheckouts(exec, files).checkout(cache.resolve(SOURCES_FOLDER), pins)
        val runtime = sources.getValue(pins.single { it.isCore })
        val queue = workers.noIsolation()
        for (pin in pins.filterNot { it.isCore }) {
            queue.submit(CompileWasmModule::class.java) {
                this.zig.set(zig)
                this.flags.set(this@CompileWasm.flags)
                this.exports.set(this@CompileWasm.exports.get() + "$GRAMMAR_ENTRY${pin.name}")
                this.runtime.set(runtime)
                this.grammar.set(sources.getValue(pin))
                this.shim.set(this@CompileWasm.shim)
                this.inlineHeader.set(this@CompileWasm.inlineHeader)
                this.lexerChunkChars.set(this@CompileWasm.lexerChunkChars)
                this.output.set(out.resolve("${pin.name}$WASM"))
                this.scratch.set(temporaryDir.resolve(pin.name))
                this.zigCache.set(cache.resolve(ZIG_CACHE_FOLDER))
            }
        }
        queue.await()
    }

    private companion object {
        const val ZIG_FOLDER = "zig"
        const val SOURCES_FOLDER = "src"
        const val ZIG_CACHE_FOLDER = "zig-cache"
        const val GRAMMAR_ENTRY = "tree_sitter_"
        const val WASM = ".wasm"
    }
}
