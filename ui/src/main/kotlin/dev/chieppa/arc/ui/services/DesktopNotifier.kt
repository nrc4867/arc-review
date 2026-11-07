package dev.chieppa.arc.ui.services

/** Where notifications go outside the window: the OS in the app, a recorder in tests. */
fun interface DesktopNotifier {
    fun show(title: String, text: String)

    companion object {
        /** Shows nothing. */
        val NONE = DesktopNotifier { _, _ -> }
    }
}
