package dev.chieppa.arc.ui.runs

import dev.chieppa.stretch.document.DocumentFormat

/** An artifact's file for the save dialog, saved as it was downloaded. */
class ArtifactFormat(override val description: String, override val extension: String) : DocumentFormat<ByteArray> {
    override fun encode(document: ByteArray): ByteArray = document

    override fun decode(bytes: ByteArray): ByteArray = bytes
}
