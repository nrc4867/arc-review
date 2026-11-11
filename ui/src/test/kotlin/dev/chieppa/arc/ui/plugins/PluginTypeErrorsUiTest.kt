package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.typecheck.TypeChecker
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.shows
import dev.chieppa.arc.ui.plugins.PluginRig.Companion.showsTag
import dev.chieppa.arc.ui.plugins.settings.PluginSettingsTags
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** A plugin's type errors show in place on its row, as board 9 draws them, and count among the problems; it still runs. */
@OptIn(ExperimentalTestApi::class)
class PluginTypeErrorsUiTest {
    private val rig = PluginRig("""{"panels":true}""", Contributions(), Capabilities(panels = true)) { "[]" }
    private val error = PluginDiagnostic(DiagnosticKind.Type, "Property 'line' does not exist on type 'GrepHit'", "src/index.tsx", 14, 20)

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    @Test
    fun aTypeErrorIsShownOnThePluginsRowWithItsPlaceAndCounted() = rig.test {
        val workbench = rig.open(this, typeChecker = TypeChecker { _, _ -> List(8) { error } })
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        assertTrue(showsTag(PluginSettingsTags.PROBLEM + PluginRig.ID))
        assertTrue(shows("src/index.tsx:14") && shows("Property 'line' does not exist on type 'GrepHit'"))
        assertTrue(onAllNodes(hasTestTag(PluginSettingsTags.PROBLEM + PluginRig.ID)).fetchSemanticsNodes().size == 5)
        assertTrue(shows("8 problems"))
        assertIs<PluginStatus.Running>(workbench.plugins.statuses[PluginRig.ID])
    }
}
