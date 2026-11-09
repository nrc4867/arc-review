package dev.chieppa.arc.ui.actions

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import dev.chieppa.stretch.theme.StretchIcons

private val icons: Map<String, ImageVector> = mapOf(
    ArcActionIds.GO_TO_ANYTHING.value to StretchIcons.Search,
    ArcActionIds.COMMANDS.value to StretchIcons.Command,
    ArcActionIds.SETTINGS.value to StretchIcons.Settings,
    ArcActionIds.ADD_SOURCE.value to StretchIcons.Plus,
    ArcActionIds.NEXT_CHANGE.value to StretchIcons.ChevronRight,
    ArcActionIds.GO_TO_DEFINITION.value to ArcIcons.Definition,
    ArcActionIds.SHOW_USAGES.value to ArcIcons.Usages,
    ArcActionIds.PREVIOUS_CHANGE.value to StretchIcons.ChevronLeft,
    ArcActionIds.NEXT_FILE.value to StretchIcons.ArrowDown,
    ArcActionIds.PREVIOUS_FILE.value to StretchIcons.ArrowUp,
    ArcActionIds.MARK_AND_ADVANCE.value to StretchIcons.Check,
    ArcActionIds.TOGGLE_REVIEWED.value to StretchIcons.Check,
    ArcActionIds.COMMENT_HERE.value to ArcIcons.Comment,
    ArcActionIds.PUSH_REVIEW.value to StretchIcons.Push,
    ArcActionIds.SET_ASIDE.value to ArcIcons.SetAside,
    ArcActionIds.FIND_IN_FILE.value to StretchIcons.Search,
    ArcActionIds.OPEN_FILE.value to StretchIcons.File,
    ArcActionIds.TOGGLE_PIN.value to StretchIcons.Pin,
    ArcActionIds.TOGGLE_FULL_FILE.value to ArcIcons.FullFile,
    ArcActionIds.TOGGLE_SPLIT.value to StretchIcons.SplitView,
    ArcActionIds.TOGGLE_WRAP.value to ArcIcons.Wrap,
    ArcActionIds.REMOVE_SOURCE.value to StretchIcons.Close,
    ArcActionIds.CLOSE_ENDED_PULLS.value to StretchIcons.Close,
    ArcActionIds.OPEN_CHANGE.value to StretchIcons.File,
    ArcActionIds.OPEN_TO_SIDE.value to StretchIcons.SplitView,
    ArcActionIds.OPEN_IN_NEW_WINDOW.value to ArcIcons.TearOut,
    ArcActionIds.OPEN_PINNED.value to StretchIcons.Pin,
    ArcActionIds.OPEN_FULL_FILE.value to ArcIcons.FullFile,
    ArcActionIds.RESTORE.value to StretchIcons.Restore,
    ArcActionIds.OPEN_REVISIONS.value to ArcIcons.Matrix,
    ArcActionIds.OPEN_PULL.value to ArcIcons.PullRequest,
    ArcActionIds.ADD_PULL.value to StretchIcons.Plus,
    ArcActionIds.OPEN_PULL_ON_GITHUB.value to StretchIcons.ExternalLink,
    ArcActionIds.COPY_PULL_LINK.value to StretchIcons.Link,
    ArcActionIds.COPY_PULL_BRANCH.value to ArcIcons.Branch,
    ArcActionIds.COPY_PULL_HEAD.value to ArcIcons.Commit,
    ArcActionIds.COPY_PULL_REFERENCE.value to ArcIcons.PullRequest,
    ArcActionIds.OPEN_LINK.value to StretchIcons.ExternalLink,
    ArcActionIds.OPEN_FILTER.value to ArcIcons.Filter,
    ArcActionIds.CONTINUE_REVIEW.value to ArcIcons.Continue,
    ArcActionIds.TOGGLE_SINCE_REVIEW.value to ArcIcons.ChangedSince,
    ArcActionIds.OPEN_CONVERSATION.value to ArcIcons.Comment,
    ArcActionIds.OPEN_SIDE_BY_SIDE.value to StretchIcons.SplitView,
    ArcActionIds.REVIEW_STAGED.value to ArcIcons.Stage,
    ArcActionIds.CLEAR_RECENT.value to StretchIcons.Close,
    ArcActionIds.FORGET_RECENT.value to StretchIcons.Close,
    ArcActionIds.HIDE_START_CARD.value to StretchIcons.EyeOff,
    ArcActionIds.MOVE_START_CARD_UP.value to StretchIcons.ArrowUp,
    ArcActionIds.MOVE_START_CARD_DOWN.value to StretchIcons.ArrowDown,
    ArcActionIds.RESTORE_START_CARDS.value to StretchIcons.Restore,
)

/** The icon for an action's icon key: a drawer action shows its drawer's icon. */
fun actionIcon(key: String): ImageVector? = icons[key] ?: RunActionTexts.icons[key] ?: ArcActionIds.drawerOf(key)?.let(ArcDrawerSlots::byId)?.icon
