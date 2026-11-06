package dev.chieppa.arc.ui.services

import java.awt.HeadlessException
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/** The system clipboard; copying does nothing where there is none, as on a machine without a display. */
object SystemClipboard : TextClipboard {
    override fun copy(text: String) {
        try {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        } catch (_: HeadlessException) {
        } catch (_: IllegalStateException) {
        }
    }
}
