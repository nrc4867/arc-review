package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.gitsource.GitSourceState
import dev.chieppa.arc.plugins.gitsource.GitUpdate
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import dev.chieppa.arc.ui.plugins.settings.GitSourceLabels
import dev.chieppa.arc.ui.plugins.settings.GitSourceRow
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A git source's row shows its pin, its last check and an update waiting, as board 9 draws it. */
@OptIn(ExperimentalTestApi::class)
class GitSourceRowUiTest {
    private val pinned = "8d1c0aa" + "0".repeat(33)
    private val next = "5e0f2b1" + "0".repeat(33)
    private val source = PluginSourceSpec.Git("https://github.com/lumen/arc-speckit.git", "v0.4.2", pinned)
    private val checked = 1_762_848_000_000L

    @Test
    fun anUpdateAskingForMoreIsAWarningWithItsReview() = runComposeUiTest {
        var reviewed: GitUpdate? = null
        var accepted = false
        val update = GitUpdate(next, "v0.5.0", listOf(Capability.Network("api.github.com")))
        setStretchContent(StretchThemes.WorkshopDark) {
            GitSourceRow(source, GitSourceState(checkedAt = checked, update = update), 1, checked, { reviewed = it }, { accepted = true }, {})
        }
        assertTrue(shows("github.com/lumen/arc-speckit"))
        assertTrue(shows("Git · v0.4.2 · pinned 8d1c0aa · checked today ${GitSourceLabels.time(checked)}"))
        assertTrue(onAllNodes(hasContentDescription("v0.5.0 asks for network: api.github.com")).fetchSemanticsNodes().isNotEmpty())
        onNodeWithText("Review update").performClick()
        assertEquals(update, reviewed)
        onNodeWithText("Accept update").performClick()
        assertTrue(accepted)
    }

    @Test
    fun aSourceThatCannotBeReadSaysSoInItsLine() = runComposeUiTest {
        setStretchContent(StretchThemes.WorkshopDark) {
            GitSourceRow(source.copy(pinned = null), GitSourceState(failed = true), 0, checked, {}, {}, {})
        }
        assertTrue(shows("could not be read"))
        assertTrue(!shows("Review update"))
    }
}
