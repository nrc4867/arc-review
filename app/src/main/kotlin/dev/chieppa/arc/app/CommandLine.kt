package dev.chieppa.arc.app

import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.cli_plugin_bad_name
import dev.chieppa.arc.design.generated.cli_plugin_created
import dev.chieppa.arc.design.generated.cli_plugin_folder_taken
import dev.chieppa.arc.design.generated.cli_plugin_not_written
import dev.chieppa.arc.design.generated.cli_usage
import dev.chieppa.arc.plugins.starter.PluginStarter
import dev.chieppa.arc.plugins.starter.StarterException
import dev.chieppa.arc.plugins.starter.StarterProblem
import dev.chieppa.arc.store.plugins.PluginFiles
import org.jetbrains.compose.resources.getString
import java.io.PrintStream
import java.nio.file.Path

/**
 * Arc's jar run as a command rather than a window: `java -jar arc-review.jar plugin new <name> [--in <folder>]` makes
 * a starter plugin in a folder of its id under the folder named (the working folder by default) and adds it to the
 * plugin sources in the data folder [dataDir], where a running Arc picks it up. The plugin is not enabled: the person
 * enables it, as any plugin. Its messages keep to ASCII, so any console's code page prints them.
 */
internal class CommandLine(private val dataDir: Path, private val workingDir: Path, private val out: PrintStream, private val err: PrintStream) {
    /** Runs [args], printing what happened, and gives the process's exit code. */
    suspend fun run(args: List<String>): Int {
        if (args.size < MIN_ARGS || args[0] != PLUGIN || args[1] != NEW) return usage()
        val rest = args.drop(MIN_ARGS)
        val inAt = rest.indexOf(IN)
        val parent = if (inAt >= 0) rest.getOrNull(inAt + 1)?.let(workingDir::resolve) ?: return usage() else workingDir
        val name = rest.filterIndexed { index, _ -> inAt < 0 || (index != inAt && index != inAt + 1) }.joinToString(" ")
        if (name.isBlank()) return usage()
        val folder = try {
            PluginStarter.create(parent, name)
        } catch (e: StarterException) {
            err.println(problem(e))
            return FAILED
        }
        val files = PluginFiles(dataDir.resolve(AppConstants.PLUGINS_FILE), dataDir.resolve(AppConstants.PLUGIN_STORE_FOLDER))
        files.saveConfig(files.loadConfig().withDirectory(folder))
        out.println(getString(Res.string.cli_plugin_created, folder.fileName.toString(), folder.parent.toString()))
        return OK
    }

    private suspend fun usage(): Int {
        err.println(getString(Res.string.cli_usage))
        return USAGE
    }

    private suspend fun problem(e: StarterException): String = when (e.problem) {
        StarterProblem.BadName -> getString(Res.string.cli_plugin_bad_name, e.detail)
        StarterProblem.FolderTaken -> getString(Res.string.cli_plugin_folder_taken, e.detail)
        StarterProblem.NotWritten -> getString(Res.string.cli_plugin_not_written, e.detail)
    }

    companion object {
        private const val PLUGIN = "plugin"
        private const val NEW = "new"
        private const val IN = "--in"
        private const val MIN_ARGS = 2
        private const val OK = 0
        private const val FAILED = 1
        private const val USAGE = 2

        /** Whether [args] ask for a command rather than the window. */
        fun handles(args: Array<String>): Boolean = args.firstOrNull() == PLUGIN
    }
}
