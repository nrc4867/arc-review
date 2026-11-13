package dev.chieppa.arc.ui.install

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.install.InstallListener
import dev.chieppa.arc.install.InstallStep
import dev.chieppa.arc.ui.testDispatchers
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class InstallViewUiTest {
    private val rows = InstallRows("0.3.0", "Windows 11")

    @Test
    fun theStepsGoByAndArcOpensOnceAllAreDone() = runComposeUiTest {
        val downloading = CompletableDeferred<Unit>()
        val dispatchers = testDispatchers()
        val controller = InstallController({ listener ->
            listener.started(InstallStep.Runtime)
            listener.runtimeBytes(50L * MB, 100L * MB)
            downloading.await()
            listener.finished(InstallStep.Runtime)
            for (step in listOf(InstallStep.Jar, InstallStep.Launcher)) {
                listener.started(step)
                listener.finished(step)
            }
        }, CoroutineScope(SupervisorJob() + dispatchers.main), dispatchers)
        var opened = 0
        setStretchContent(StretchThemes.WorkshopDark) { InstallView(controller, rows) { opened++ } }
        controller.start()
        waitForIdle()
        onNodeWithTag(InstallTags.row(InstallStep.Runtime, StepStatus.Running)).assertExists()
        onNodeWithText("50 of 100 MB").assertExists()
        onNodeWithTag(InstallTags.row(InstallStep.Jar, StepStatus.Waiting)).assertExists()
        onNodeWithText("Open Arc").assertIsNotEnabled()

        downloading.complete(Unit)
        waitForIdle()
        InstallStep.entries.forEach { onNodeWithTag(InstallTags.row(it, StepStatus.Done)).assertExists() }
        onNodeWithText("Start menu shortcut").assertExists()
        onNodeWithText("Arc 0.3.0").assertExists()
        onNodeWithText("Open Arc").assertIsEnabled().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun aFailedStepIsMarkedAndTriedAgain() = runComposeUiTest {
        var attempts = 0
        val dispatchers = testDispatchers()
        val controller = InstallController({ listener: InstallListener ->
            attempts++
            listener.started(InstallStep.Runtime)
            if (attempts == 1) throw IOException("offline")
            InstallStep.entries.forEach { if (it != InstallStep.Runtime) listener.started(it); listener.finished(it) }
        }, CoroutineScope(SupervisorJob() + dispatchers.main), dispatchers)
        setStretchContent(StretchThemes.WorkshopDark) { InstallView(controller, rows) {} }
        controller.start()
        waitForIdle()
        onNodeWithTag(InstallTags.row(InstallStep.Runtime, StepStatus.Failed)).assertExists()
        onNodeWithText("Try again").performClick()
        waitForIdle()
        InstallStep.entries.forEach { onNodeWithTag(InstallTags.row(it, StepStatus.Done)).assertExists() }
        assertEquals(2, attempts)
    }

    private companion object {
        const val MB = 1024L * 1024L
    }
}
