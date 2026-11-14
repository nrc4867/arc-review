package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.ui.plugins.settings.PluginSettingsTags
import dev.chieppa.arc.ui.util.FolderChooser
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NewPluginUiTest {
    private val rig = PluginRig("""{"panels":true}""", Contributions(), Capabilities(panels = true)) { "[]" }

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    @Test
    fun newPluginMakesAStarterAndAddsItAsASourceLeftOff() = rig.test {
        val workbench = rig.open(this)
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        onNodeWithTag(PluginSettingsTags.NEW_PARENT).performTextInput(rig.dir.resolve("made").toString())
        onNodeWithTag(PluginSettingsTags.NEW_NAME).performTextInput("Spec View")
        waitForIdle()
        onNodeWithTag(PluginSettingsTags.NEW_CREATE).performClick()
        waitForIdle()
        val folder = rig.dir.resolve("made/spec-view").toAbsolutePath().normalize()
        assertTrue(Files.isRegularFile(folder.resolve("plugin.json")))
        assertEquals(PluginSourceSpec.Directory(folder.toString()), workbench.plugins.config.sources.last())
        assertEquals(setOf(PluginRig.ID), workbench.plugins.config.enabled)
        assertTrue(workbench.plugins.entries.any { it.id == "spec-view" && !it.enabled })
    }

    @Test
    fun theFolderButtonsAskForAFolderStartingAtTheTypedOneAndFillTheField() = rig.test {
        val asked = mutableListOf<Pair<String, String?>>()
        val chosen = rig.dir.resolve("chosen").toString()
        val workbench = rig.open(this, folders = FolderChooser { title, start -> asked += title to start; chosen })
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        onNodeWithTag(PluginSettingsTags.NEW_PARENT).performTextInput(rig.dir.toString())
        onAllNodesWithContentDescription("Browse for a folder")[1].performClick()
        waitForIdle()
        assertEquals(listOf<Pair<String, String?>>("Choose a folder" to rig.dir.toString()), asked)
        onNodeWithTag(PluginSettingsTags.NEW_PARENT).assertTextEquals(chosen)
        onAllNodesWithContentDescription("Browse for a folder")[0].performClick()
        waitForIdle()
        assertEquals<Pair<String, String?>>("Choose a folder" to null, asked.last(), "an empty field starts the dialog where it likes")
        onNodeWithTag(PluginSettingsTags.ADD_SOURCE).assertTextEquals(chosen)
    }

    @Test
    fun aFolderThatIsNoFullPathIsMarkedAndCannotBeUsed() = rig.test {
        val workbench = rig.open(this)
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        onNodeWithTag(PluginSettingsTags.NEW_PARENT).performTextInput("IdeaProjects/arc")
        onNodeWithTag(PluginSettingsTags.NEW_NAME).performTextInput("Spec View")
        waitForIdle()
        onNodeWithTag(PluginSettingsTags.NEW_PARENT).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        onNodeWithTag(PluginSettingsTags.NEW_CREATE).assertIsNotEnabled()
    }
}
