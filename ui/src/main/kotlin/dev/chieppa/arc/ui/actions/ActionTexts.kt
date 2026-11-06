package dev.chieppa.arc.ui.actions

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_add_source
import dev.chieppa.arc.design.generated.action_close_pane
import dev.chieppa.arc.design.generated.action_find_in_file
import dev.chieppa.arc.design.generated.action_go_to_anything
import dev.chieppa.arc.design.generated.action_mark_and_advance
import dev.chieppa.arc.design.generated.action_next_change
import dev.chieppa.arc.design.generated.action_next_file
import dev.chieppa.arc.design.generated.action_open_file
import dev.chieppa.arc.design.generated.action_previous_change
import dev.chieppa.arc.design.generated.action_previous_file
import dev.chieppa.arc.design.generated.action_quit
import dev.chieppa.arc.design.generated.action_reset_layout
import dev.chieppa.arc.design.generated.action_set_aside
import dev.chieppa.arc.design.generated.action_settings
import dev.chieppa.arc.design.generated.action_toggle_full_file
import dev.chieppa.arc.design.generated.action_toggle_pin
import dev.chieppa.arc.design.generated.action_toggle_reviewed
import dev.chieppa.arc.design.generated.action_toggle_split
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val texts: Map<String, StringResource> = mapOf(
    ArcActionIds.GO_TO_ANYTHING.value to Res.string.action_go_to_anything,
    ArcActionIds.SETTINGS.value to Res.string.action_settings,
    ArcActionIds.RESET_LAYOUT.value to Res.string.action_reset_layout,
    ArcActionIds.QUIT.value to Res.string.action_quit,
    ArcActionIds.ADD_SOURCE.value to Res.string.action_add_source,
    ArcActionIds.NEXT_FILE.value to Res.string.action_next_file,
    ArcActionIds.PREVIOUS_FILE.value to Res.string.action_previous_file,
    ArcActionIds.NEXT_CHANGE.value to Res.string.action_next_change,
    ArcActionIds.PREVIOUS_CHANGE.value to Res.string.action_previous_change,
    ArcActionIds.MARK_AND_ADVANCE.value to Res.string.action_mark_and_advance,
    ArcActionIds.TOGGLE_REVIEWED.value to Res.string.action_toggle_reviewed,
    ArcActionIds.SET_ASIDE.value to Res.string.action_set_aside,
    ArcActionIds.FIND_IN_FILE.value to Res.string.action_find_in_file,
    ArcActionIds.OPEN_FILE.value to Res.string.action_open_file,
    ArcActionIds.TOGGLE_PIN.value to Res.string.action_toggle_pin,
    ArcActionIds.TOGGLE_FULL_FILE.value to Res.string.action_toggle_full_file,
    ArcActionIds.TOGGLE_SPLIT.value to Res.string.action_toggle_split,
    ArcActionIds.CLOSE_PANE.value to Res.string.action_close_pane,
) + ArcDrawerSlots.all.associate { ArcActionIds.toggleDrawer(it.id).value to it.title }

/** Turns an action's text key into its label, for Stretch's `LocalActionText`. */
@Composable
fun actionText(): (String) -> String {
    val labels = texts.mapValues { (_, resource) -> stringResource(resource) }
    return { key -> labels[key] ?: key }
}
