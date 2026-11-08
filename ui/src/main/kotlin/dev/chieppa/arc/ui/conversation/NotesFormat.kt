package dev.chieppa.arc.ui.conversation

import dev.chieppa.stretch.document.DocumentFormat

/** A source's notes as a Markdown file, for the save dialog that exports them. */
class NotesFormat(override val description: String) : DocumentFormat<String> {
    override val extension: String = EXTENSION

    override fun encode(document: String): ByteArray = document.toByteArray(Charsets.UTF_8)

    override fun decode(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)

    private companion object {
        const val EXTENSION = "md"
    }
}
