package dev.chieppa.arc.install

/** Hears an install go: each step as it starts and finishes, and the runtime's download as it arrives. */
interface InstallListener {
    fun started(step: InstallStep) {}

    fun finished(step: InstallStep) {}

    /** [done] bytes of the runtime's archive have arrived, of [total] when the server says. */
    fun runtimeBytes(done: Long, total: Long?) {}

    companion object {
        /** A listener that hears nothing. */
        val None = object : InstallListener {}
    }
}
