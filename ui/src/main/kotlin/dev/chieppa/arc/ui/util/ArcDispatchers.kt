package dev.chieppa.arc.ui.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Where the window's work runs: [main] for work that reads and changes the window's state, [default] for computing
 * off it, such as diffs and pictures, and [io] for blocking reads and writes. Every part of the window takes these
 * from its `Workbench`, so a test can run all of it on one dispatcher it controls.
 */
class ArcDispatchers(val main: CoroutineDispatcher, val default: CoroutineDispatcher, val io: CoroutineDispatcher) {
    companion object {
        /** The app's: the Swing main thread, the shared pool and the pool for blocking work. */
        val Real = ArcDispatchers(Dispatchers.Main, Dispatchers.Default, Dispatchers.IO)

        /** All of the window's work on [dispatcher]. */
        fun on(dispatcher: CoroutineDispatcher) = ArcDispatchers(dispatcher, dispatcher, dispatcher)
    }
}
