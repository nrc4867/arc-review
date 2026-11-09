package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.artifacts.ArtifactKind
import java.nio.file.Path

/** What an artifact holds that can be shown: the [entry] picked from it, its kind, its bytes, and the downloaded [file]. */
class ArtifactContent(val entry: String, val kind: ArtifactKind, val bytes: ByteArray, val file: Path, val entries: List<String>) {
    fun text(): String = bytes.toString(Charsets.UTF_8)
}
