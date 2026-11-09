package dev.chieppa.arc.app

/**
 * Runs one step of closing the app, printing anything it throws instead of letting it stop the window from closing,
 * such as a class that can no longer be loaded because the app was rebuilt while it ran. Inline, so the guard itself
 * loads no class at exit.
 */
@Suppress("TooGenericExceptionCaught")
inline fun exitStep(step: () -> Unit) {
    try {
        step()
    } catch (failure: Throwable) {
        failure.printStackTrace()
    }
}
