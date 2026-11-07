package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

/** How often ages on screen move on. */
private const val TICK_MILLIS = 30_000L

/** The time now, in epoch milliseconds, moving on every half minute so ages shown with it stay true. */
@Composable
fun currentTime(): Long = produceState(System.currentTimeMillis()) {
    while (true) {
        delay(TICK_MILLIS)
        value = System.currentTimeMillis()
    }
}.value
