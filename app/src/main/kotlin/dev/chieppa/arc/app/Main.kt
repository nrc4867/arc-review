package dev.chieppa.arc.app

import androidx.compose.ui.window.application
import dev.chieppa.arc.install.SelfLocation
import dev.chieppa.arc.tools.SystemTrust
import dev.chieppa.stretch.devtools.DevTools
import dev.chieppa.stretch.document.appDataDirectory
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    SystemTrust.install()
    val dataDir = appDataDirectory(AppConstants.APP_ID)
    if (CommandLine.handles(args)) {
        val code = runBlocking { CommandLine(dataDir, Path.of("").toAbsolutePath(), System.out, System.err).run(args.toList()) }
        exitProcess(code)
    }
    DevTools.startIfRequested()
    val self = SelfLocation.current()
    val installation = Installation(dataDir)
    when (installation.decide(self)) {
        StartDecision.Run -> runArc(dataDir, installation, self)
        StartDecision.OpenInstalled -> {
            installation.launcher.start()
            exitProcess(0)
        }
        is StartDecision.Install -> {
            val jar = checkNotNull(self.jar)
            application {
                InstallWindow(
                    version = self.version.orEmpty(),
                    install = { listener -> installation.installer.install(jar, listener) },
                    onOpen = {
                        installation.launcher.start()
                        exitApplication()
                    },
                    onClose = ::exitApplication,
                )
            }
        }
    }
}
