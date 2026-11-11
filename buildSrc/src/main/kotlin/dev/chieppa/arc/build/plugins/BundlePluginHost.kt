package dev.chieppa.arc.build.plugins

import dev.chieppa.arc.build.natives.HostSystem
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.util.Properties
import javax.inject.Inject

/**
 * Bundles the plugin host, the TypeScript in [sources] from [entry], with React, react-reconciler and Arc's SDK into
 * one JavaScript file at `<outputDir>/<resourcePath>`, using the esbuild and npm packages [pins] pins, kept in
 * [cacheDir]; each of [copies] (a file inside an installed package, to the name it ships as) is copied beside it,
 * with the files [packageFiles] and [sourceFiles] match, and an [INDEX] naming every file shipped. They are resources
 * Arc ships and writes out for the plugins' Node process to run and type check with.
 */
@CacheableTask
abstract class BundlePluginHost : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val pins: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: DirectoryProperty

    /** The entry file, relative to [sources]. */
    @get:Input
    abstract val entry: Property<String>

    /** The npm packages bundled in. */
    @get:Input
    abstract val packages: ListProperty<String>

    @get:Input
    abstract val resourcePath: Property<String>

    /** Files of the installed packages shipped beside the host as they are, by path under `node_modules`, to their names. */
    @get:Input
    abstract val copies: MapProperty<String, String>

    /** Files of the installed packages by a glob under `node_modules` (`*` in the file name only), to a folder beside the host. */
    @get:Input
    abstract val packageFiles: MapProperty<String, String>

    /** Files of [sources] by a glob relative to it (`*` in the file name only), to a folder beside the host. */
    @get:Input
    abstract val sourceFiles: MapProperty<String, String>

    @get:Internal
    abstract val cacheDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val exec: ExecOperations

    @TaskAction
    fun bundle() {
        val properties = Properties().apply { pins.get().asFile.inputStream().use { load(it) } }
        val cache = PluginToolsCache(cacheDir.get().asFile, properties, exec)
        val esbuild = cache.esbuild()
        val modules = cache.nodeModules(packages.get())
        val out = outputDir.get().asFile
        out.deleteRecursively()
        val target = File(out, resourcePath.get())
        exec.exec {
            workingDir = sources.get().asFile
            environment(NODE_PATH, modules.path)
            commandLine(
                esbuild.path,
                entry.get(),
                "--bundle",
                "--platform=node",
                "--format=cjs",
                "--target=node22",
                "--define:process.env.NODE_ENV=$PRODUCTION",
                "--log-level=warning",
                "--legal-comments=eof",
                "--outfile=${target.path}",
            )
        }
        val folder = target.parentFile
        copies.get().forEach { (from, name) -> modules.resolve(from).copyTo(File(folder, name), overwrite = true) }
        packageFiles.get().forEach { (glob, to) -> copyMatching(modules, glob, File(folder, to)) }
        sourceFiles.get().forEach { (glob, to) -> copyMatching(sources.get().asFile, glob, File(folder, to)) }
        val shipped = folder.walkTopDown().filter { it.isFile }.map { it.relativeTo(folder).invariantSeparatorsPath }.sorted()
        File(folder, INDEX).writeText(shipped.joinToString(LINE, postfix = LINE))
    }

    private fun copyMatching(root: File, glob: String, into: File) {
        val dir = root.resolve(glob.substringBeforeLast('/', ""))
        val name = Regex(glob.substringAfterLast('/').split('*').joinToString(".*") { Regex.escape(it) })
        val files = dir.listFiles().orEmpty().filter { it.isFile && name.matches(it.name) }
        files.forEach { it.copyTo(File(into, it.name), overwrite = true) }
    }

    companion object {
        /** The file beside the host naming every file shipped with it, one path per line. */
        const val INDEX = "files.txt"

        private const val NODE_PATH = "NODE_PATH"
        private const val LINE = "\n"

        // A JSON string; Windows' argument parsing drops bare quotes, so there they are escaped.
        private val PRODUCTION = if (HostSystem.isWindows) "\\\"production\\\"" else "\"production\""
    }
}
