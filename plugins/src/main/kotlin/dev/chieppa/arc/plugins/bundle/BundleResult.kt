package dev.chieppa.arc.plugins.bundle

import dev.chieppa.arc.plugins.host.PluginDiagnostic
import java.nio.file.Path

/** A plugin's code transpiled into one [Bundled] file, or the [Failed] reasons it could not be. */
sealed interface BundleResult {
    data class Bundled(val file: Path) : BundleResult

    data class Failed(val diagnostics: List<PluginDiagnostic>) : BundleResult
}
