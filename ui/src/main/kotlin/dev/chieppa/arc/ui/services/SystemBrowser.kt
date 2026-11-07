package dev.chieppa.arc.ui.services

import java.awt.Desktop
import java.awt.HeadlessException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

/** The system browser; opening does nothing where there is none, as on a machine without a display. */
object SystemBrowser : LinkOpener {
    override fun open(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop().browse(URI(url))
        } catch (_: HeadlessException) {
        } catch (_: IOException) {
        } catch (_: URISyntaxException) {
        } catch (_: UnsupportedOperationException) {
        }
    }
}
