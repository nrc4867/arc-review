package dev.chieppa.arc.build.wasm

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import javax.inject.Inject

/**
 * Compiles one grammar with tree-sitter's runtime and the shim into a WebAssembly module with `zig cc`, from a copy of
 * the grammar's sources whose `parser.c` has its lexers split ([LexerSplitter]).
 */
abstract class CompileWasmModule : WorkAction<CompileWasmModuleParameters> {
    @get:Inject
    abstract val exec: ExecOperations

    @get:Inject
    abstract val files: FileSystemOperations

    override fun execute() {
        val p = parameters
        val scratch = p.scratch.get().asFile
        files.delete { delete(scratch) }
        scratch.mkdirs()
        // Only parser.c is rewritten; the scanner compiles in place, so its relative includes still resolve.
        val sources = p.grammar.get().asFile
        val parser = scratch.resolve(PARSER)
        parser.writeText(LexerSplitter.split(sources.resolve(PARSER).readText(), p.lexerChunkChars.get()))
        val runtime = p.runtime.get().asFile
        val built = scratch.resolve(p.output.get().asFile.name)
        val cache = p.zigCache.get().asFile.path
        val inputs = listOf(runtime.resolve(RUNTIME_SOURCE), p.shim.get().asFile, parser) +
            listOf(sources.resolve(SCANNER)).filter { it.isFile }
        exec.exec {
            workingDir = scratch
            environment(ZIG_GLOBAL_CACHE_DIR, cache)
            environment(ZIG_LOCAL_CACHE_DIR, cache)
            commandLine(
                listOf(p.zig.get().asFile.path, "cc", "-target", WasmFlags.TARGET) + p.flags.get() +
                    listOf("-include", p.inlineHeader.get().asFile.path) +
                    listOf(runtime.resolve(INCLUDE), runtime.resolve(SRC), sources).flatMap { listOf("-I", it.path) } +
                    inputs.map { it.path } + p.exports.get().map { "-Wl,--export=$it" } + listOf("-o", built.path),
            )
        }
        val output = p.output.get().asFile
        output.parentFile.mkdirs()
        built.copyTo(output, overwrite = true)
    }

    private companion object {
        const val ZIG_GLOBAL_CACHE_DIR = "ZIG_GLOBAL_CACHE_DIR"
        const val ZIG_LOCAL_CACHE_DIR = "ZIG_LOCAL_CACHE_DIR"
        const val PARSER = "parser.c"
        const val SCANNER = "scanner.c"
        const val RUNTIME_SOURCE = "src/lib.c"
        const val INCLUDE = "include"
        const val SRC = "src"
    }
}
