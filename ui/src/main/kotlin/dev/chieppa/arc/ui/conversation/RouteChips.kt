package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_in_summary
import dev.chieppa.arc.design.generated.staged_on_commit
import dev.chieppa.arc.design.generated.staged_on_conversation
import dev.chieppa.arc.design.generated.staged_on_file
import dev.chieppa.arc.design.generated.staged_reply
import dev.chieppa.arc.design.generated.staged_state
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.stringResource

/** The label and icon of a staged comment's chip, which says how it goes up. */
@Composable
fun routeChip(route: StagedRoute): Pair<String, ImageVector> = when (route) {
    StagedRoute.Line -> stringResource(Res.string.staged_state) to ArcIcons.Stage
    StagedRoute.File -> stringResource(Res.string.staged_on_file) to StretchIcons.File
    StagedRoute.Summary -> stringResource(Res.string.staged_in_summary) to StretchIcons.Quote
    StagedRoute.Reply -> stringResource(Res.string.staged_reply) to ArcIcons.Comment
    StagedRoute.Conversation -> stringResource(Res.string.staged_on_conversation) to ArcIcons.Comment
    StagedRoute.Commit -> stringResource(Res.string.staged_on_commit) to ArcIcons.Commit
}
