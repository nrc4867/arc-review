package dev.chieppa.arc.build.natives

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import javax.inject.Inject

/**
 * Compiles one shared library with `zig cc` in its own scratch folder and copies only the library to its output, so
 * the import libraries and debug files zig writes beside Windows libraries never ship.
 */
abstract class CompileLibrary : WorkAction<CompileLibraryParameters> {
    @get:Inject
    abstract val exec: ExecOperations

    @get:Inject
    abstract val files: FileSystemOperations

    override fun execute() {
        val p = parameters
        val scratch = p.scratch.get().asFile
        files.delete { delete(scratch) }
        scratch.mkdirs()
        val output = p.output.get().asFile
        val built = scratch.resolve(output.name)
        val cache = p.zigCache.get().asFile.path
        exec.exec {
            workingDir = scratch
            environment(ZIG_GLOBAL_CACHE_DIR, cache)
            environment(ZIG_LOCAL_CACHE_DIR, cache)
            commandLine(
                listOf(p.zig.get().asFile.path, "cc", "-target", p.target.get()) + p.flags.get() +
                    p.includes.get().flatMap { listOf("-I", it) } + p.sources.get() + listOf("-o", built.path),
            )
        }
        output.parentFile.mkdirs()
        built.copyTo(output, overwrite = true)
    }

    private companion object {
        const val ZIG_GLOBAL_CACHE_DIR = "ZIG_GLOBAL_CACHE_DIR"
        const val ZIG_LOCAL_CACHE_DIR = "ZIG_LOCAL_CACHE_DIR"
    }
}
