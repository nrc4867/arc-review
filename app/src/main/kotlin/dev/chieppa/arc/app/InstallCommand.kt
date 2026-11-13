package dev.chieppa.arc.app

import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.cli_install_done
import dev.chieppa.arc.design.generated.cli_install_failed
import dev.chieppa.arc.design.generated.cli_install_no_jar
import dev.chieppa.arc.design.generated.cli_uninstall_done
import dev.chieppa.arc.design.generated.cli_uninstall_left
import dev.chieppa.arc.install.SelfLocation
import org.jetbrains.compose.resources.getString
import java.io.IOException
import java.io.PrintStream

/**
 * `install [--no-launch]` and `uninstall` on the command line, with no window: install puts the jar it runs from, its
 * runtime and the launcher in [installation] and starts Arc through the launcher unless told not to; uninstall takes
 * them away and leaves the person's data.
 */
internal class InstallCommand(private val installation: Installation, private val self: SelfLocation, private val out: PrintStream, private val err: PrintStream) {
    suspend fun install(launch: Boolean): Int {
        val jar = self.jar ?: return failed(getString(Res.string.cli_install_no_jar))
        val installed = try {
            installation.installer.install(jar)
        } catch (e: IOException) {
            return failed(getString(Res.string.cli_install_failed, e.message.orEmpty()))
        }
        out.println(getString(Res.string.cli_install_done, installed.version, installation.layout.dataDir.toString()))
        if (launch) installation.launcher.start()
        return CommandLine.OK
    }

    suspend fun uninstall(): Int {
        val left = installation.uninstaller.uninstall()
        if (left.isEmpty()) {
            out.println(getString(Res.string.cli_uninstall_done, installation.layout.dataDir.toString()))
            return CommandLine.OK
        }
        left.forEach { err.println(getString(Res.string.cli_uninstall_left, it.toString())) }
        return CommandLine.FAILED
    }

    private fun failed(message: String): Int {
        err.println(message)
        return CommandLine.FAILED
    }
}
