package dev.chieppa.arc.ui.actions

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_go_to_anything
import dev.chieppa.arc.design.generated.action_quit
import dev.chieppa.arc.design.generated.action_reset_layout
import dev.chieppa.arc.design.generated.action_settings
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val texts: Map<String, StringResource> = mapOf(
    ArcActionIds.GO_TO_ANYTHING.value to Res.string.action_go_to_anything,
    ArcActionIds.SETTINGS.value to Res.string.action_settings,
    ArcActionIds.RESET_LAYOUT.value to Res.string.action_reset_layout,
    ArcActionIds.QUIT.value to Res.string.action_quit,
) + ArcDrawerSlots.all.associate { ArcActionIds.toggleDrawer(it.id).value to it.title }

/** Turns an action's text key into its label, for Stretch's `LocalActionText`. */
@Composable
fun actionText(): (String) -> String {
    val labels = texts.mapValues { (_, resource) -> stringResource(resource) }
    return { key -> labels[key] ?: key }
}
