package dev.chieppa.arc.ui.install

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.install_step_jar
import dev.chieppa.arc.design.generated.install_step_launcher_mac
import dev.chieppa.arc.design.generated.install_step_launcher_other
import dev.chieppa.arc.design.generated.install_step_launcher_windows
import dev.chieppa.arc.design.generated.install_step_runtime
import dev.chieppa.arc.install.InstallStep
import org.jetbrains.compose.resources.stringResource

/** The install window's row labels: the runtime, Arc's [version], and the launcher as this [os] has it. */
class InstallRows(private val version: String, private val os: String = System.getProperty(OS_NAME)) {
    @Composable
    fun label(step: InstallStep): String = when (step) {
        InstallStep.Runtime -> stringResource(Res.string.install_step_runtime)
        InstallStep.Jar -> stringResource(Res.string.install_step_jar, version)
        InstallStep.Launcher -> when {
            os.startsWith(WINDOWS) -> stringResource(Res.string.install_step_launcher_windows)
            os.startsWith(MAC) -> stringResource(Res.string.install_step_launcher_mac)
            else -> stringResource(Res.string.install_step_launcher_other)
        }
    }

    private companion object {
        const val OS_NAME = "os.name"
        const val WINDOWS = "Windows"
        const val MAC = "Mac"
    }
}
