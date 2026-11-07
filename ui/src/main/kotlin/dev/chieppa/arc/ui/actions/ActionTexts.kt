package dev.chieppa.arc.ui.actions

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_add_source
import dev.chieppa.arc.design.generated.action_close_pane
import dev.chieppa.arc.design.generated.action_find_in_file
import dev.chieppa.arc.design.generated.action_go_to_anything
import dev.chieppa.arc.design.generated.action_commands
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
import dev.chieppa.arc.design.generated.action_remove_source
import dev.chieppa.arc.design.generated.action_activate_source
import dev.chieppa.arc.design.generated.action_copy_source_ref
import dev.chieppa.arc.design.generated.action_open_change
import dev.chieppa.arc.design.generated.action_open_in_new_tab
import dev.chieppa.arc.design.generated.action_open_to_side
import dev.chieppa.arc.design.generated.action_open_in_new_window
import dev.chieppa.arc.design.generated.action_open_pinned
import dev.chieppa.arc.design.generated.action_open_full_file
import dev.chieppa.arc.design.generated.action_restore
import dev.chieppa.arc.design.generated.action_copy_path
import dev.chieppa.arc.design.generated.action_copy_file_ref
import dev.chieppa.arc.design.generated.action_expand_all
import dev.chieppa.arc.design.generated.action_collapse_all
import dev.chieppa.arc.design.generated.action_toggle_wrap
import dev.chieppa.arc.design.generated.action_open_revisions
import dev.chieppa.arc.design.generated.action_open_pull
import dev.chieppa.arc.design.generated.action_add_pull
import dev.chieppa.arc.design.generated.action_open_pull_in_new_review
import dev.chieppa.arc.design.generated.action_open_pull_review
import dev.chieppa.arc.design.generated.action_open_pull_on_github
import dev.chieppa.arc.design.generated.action_copy_pull_link
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val texts: Map<String, StringResource> = mapOf(
    ArcActionIds.GO_TO_ANYTHING.value to Res.string.action_go_to_anything,
    ArcActionIds.COMMANDS.value to Res.string.action_commands,
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
    ArcActionIds.REMOVE_SOURCE.value to Res.string.action_remove_source,
    ArcActionIds.ACTIVATE_SOURCE.value to Res.string.action_activate_source,
    ArcActionIds.COPY_SOURCE_REF.value to Res.string.action_copy_source_ref,
    ArcActionIds.OPEN_CHANGE.value to Res.string.action_open_change,
    ArcActionIds.OPEN_IN_NEW_TAB.value to Res.string.action_open_in_new_tab,
    ArcActionIds.OPEN_TO_SIDE.value to Res.string.action_open_to_side,
    ArcActionIds.OPEN_IN_NEW_WINDOW.value to Res.string.action_open_in_new_window,
    ArcActionIds.OPEN_PINNED.value to Res.string.action_open_pinned,
    ArcActionIds.OPEN_FULL_FILE.value to Res.string.action_open_full_file,
    ArcActionIds.RESTORE.value to Res.string.action_restore,
    ArcActionIds.COPY_PATH.value to Res.string.action_copy_path,
    ArcActionIds.COPY_FILE_REF.value to Res.string.action_copy_file_ref,
    ArcActionIds.EXPAND_ALL.value to Res.string.action_expand_all,
    ArcActionIds.COLLAPSE_ALL.value to Res.string.action_collapse_all,
    ArcActionIds.TOGGLE_WRAP.value to Res.string.action_toggle_wrap,
    ArcActionIds.OPEN_REVISIONS.value to Res.string.action_open_revisions,
    ArcActionIds.OPEN_PULL.value to Res.string.action_open_pull,
    ArcActionIds.ADD_PULL.value to Res.string.action_add_pull,
    ArcActionIds.OPEN_PULL_IN_NEW_REVIEW.value to Res.string.action_open_pull_in_new_review,
    ArcActionIds.OPEN_PULL_REVIEW.value to Res.string.action_open_pull_review,
    ArcActionIds.OPEN_PULL_ON_GITHUB.value to Res.string.action_open_pull_on_github,
    ArcActionIds.COPY_PULL_LINK.value to Res.string.action_copy_pull_link,
) + ArcDrawerSlots.all.associate { ArcActionIds.toggleDrawer(it.id).value to it.title }

/** Turns an action's text key into its label, for Stretch's `LocalActionText`. */
@Composable
fun actionText(): (String) -> String {
    val labels = texts.mapValues { (_, resource) -> stringResource(resource) }
    return { key -> labels[key] ?: key }
}
