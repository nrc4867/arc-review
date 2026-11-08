package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.assertTrue

/**
 * The window's work on this test's scheduler: it runs at once until it waits, and its delays run on the test's clock,
 * so `waitForIdle` and `mainClock` cover it.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
fun ComposeUiTest.testDispatchers(): ArcDispatchers = ArcDispatchers.on(UnconfinedTestDispatcher(mainClock.scheduler))

/** Lets the window settle, then checks [condition]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertWhenIdle(condition: () -> Boolean) {
    waitForIdle()
    assertTrue(condition())
}
