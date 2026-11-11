package dev.chieppa.arc.build.plugins

import dev.chieppa.arc.build.natives.HostSystem
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
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
 * [cacheDir]. The file is a resource Arc ships and writes out for each plugin process to run.
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
    }

    private companion object {
        const val NODE_PATH = "NODE_PATH"

        // A JSON string; Windows' argument parsing drops bare quotes, so there they are escaped.
        val PRODUCTION = if (HostSystem.isWindows) "\\\"production\\\"" else "\"production\""
    }
}
