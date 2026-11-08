package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.verdict_approve
import dev.chieppa.arc.design.generated.verdict_comment
import dev.chieppa.arc.design.generated.verdict_request_changes
import org.jetbrains.compose.resources.stringResource

/** Each verdict's name. */
@Composable
fun verdictLabels(): Map<Verdict, String> = mapOf(
    Verdict.Comment to stringResource(Res.string.verdict_comment),
    Verdict.Approve to stringResource(Res.string.verdict_approve),
    Verdict.RequestChanges to stringResource(Res.string.verdict_request_changes),
)
