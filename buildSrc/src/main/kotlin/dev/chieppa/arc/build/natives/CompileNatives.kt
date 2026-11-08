package dev.chieppa.arc.build.natives

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
import java.io.File
import javax.inject.Inject

/**
 * Builds the tree-sitter core (with the shim) and every grammar in [grammars] as shared libraries for each of
 * [targets], into `<outputDir>/<resourcePath>/<platform>/`. Zig and the sources are fetched into [cacheDir] only when
 * the libraries are actually compiled, so a build-cache hit needs neither. The libraries compile in parallel.
 */
@CacheableTask
abstract class CompileNatives : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val grammars: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val shim: RegularFileProperty

    /** Platform names, such as `windows-x64`. */
    @get:Input
    abstract val targets: ListProperty<String>

    @get:Input
    abstract val zigVersion: Property<String>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val resourcePath: Property<String>

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
        flags.convention(NativeFlags.COMMON)
    }

    @TaskAction
    fun compile() {
        val out = outputDir.get().asFile
        files.delete { delete(out) }
        out.mkdirs()
        val selected = targets.get().map(NativeTarget::of)
        if (selected.isEmpty()) return
        val cache = cacheDir.get().asFile
        val zig = ZigInstaller(exec, files, archives).install(cache.resolve(ZIG_FOLDER))
        val pins = GrammarPins.read(grammars.get().asFile)
        val sources = GrammarCheckouts(exec, files).checkout(cache.resolve(SOURCES_FOLDER), pins)
        val queue = workers.noIsolation()
        val shimPath = shim.get().asFile.path
        val zigCache = cache.resolve(ZIG_CACHE_FOLDER)
        val compileFlags = flags.get()
        for (target in selected) {
            val folder = out.resolve(resourcePath.get()).resolve(target.platform)
            for (pin in pins) {
                val src = sources.getValue(pin)
                queue.submit(CompileLibrary::class.java) {
                    this.zig.set(zig)
                    this.target.set(target.zigTarget)
                    this.flags.set(compileFlags + target.flags)
                    this.includes.set(includesOf(pin, src))
                    this.sources.set(sourcesOf(pin, src, shimPath))
                    this.output.set(folder.resolve(target.fileName(pin.libraryName)))
                    this.scratch.set(temporaryDir.resolve(target.platform).resolve(pin.name))
                    this.zigCache.set(zigCache)
                }
            }
        }
        queue.await()
    }

    private fun includesOf(pin: GrammarPin, src: File): List<String> =
        if (pin.isCore) listOf(src.resolve("include").path, src.resolve("src").path) else listOf(src.path)

    private fun sourcesOf(pin: GrammarPin, src: File, shimPath: String): List<String> =
        if (pin.isCore) {
            listOf(src.resolve("src/lib.c").path, shimPath)
        } else {
            listOf(src.resolve("parser.c"), src.resolve("scanner.c")).filter { it.isFile }.map { it.path }
        }

    private companion object {
        const val ZIG_FOLDER = "zig"
        const val SOURCES_FOLDER = "src"
        const val ZIG_CACHE_FOLDER = "zig-cache"
    }
}
