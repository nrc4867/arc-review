package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.SettingSpec
import dev.chieppa.arc.plugins.protocol.SettingType
import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A secret setting's field on a plugin's settings page is masked, and highlighted where secrets do not last. */
@OptIn(ExperimentalTestApi::class)
class PluginSecretFieldUiTest {
    private val rig = PluginRig(
        """{"panels":true}""",
        Contributions(settings = mapOf("token" to SettingSpec(SettingType.Secret)), settingsPage = true),
        Capabilities(panels = true),
    ) { """[{"op":"create","id":1,"type":"Field","props":{"value":"ghp_abc","secret":true,"placeholder":"Token","onChange":{"${'$'}handler":true}}},{"op":"append","parent":0,"child":1}]""" }

    @AfterTest
    fun cleanUp() = rig.cleanUp()

    private fun ComposeUiTest.secretField(secrets: SecretStore): SemanticsMatcher {
        val workbench = rig.open(this, secrets = secrets)
        workbench.panes.openSettings()
        waitForIdle()
        onNodeWithText("Plugins").performClick()
        waitForIdle()
        val field = onAllNodes(hasSetTextAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)).fetchSemanticsNodes()
        assertEquals(1, field.size)
        assertTrue("ghp_abc" !in field.single().config.getOrElseNullable(SemanticsProperties.EditableText) { null }?.text.orEmpty())
        return hasSetTextAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
    }

    @Test
    fun aSecretFieldIsMasked() = rig.test {
        val highlighted = secretField(MemorySecretStore(lasting = true))
        assertTrue(onAllNodes(highlighted).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun whereSecretsDoNotLastTheFieldIsHighlighted() = rig.test {
        val highlighted = secretField(MemorySecretStore(lasting = false))
        assertEquals(1, onAllNodes(highlighted).fetchSemanticsNodes().size)
    }
}
