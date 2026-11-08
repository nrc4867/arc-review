package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.markdown.MarkdownView

/**
 * What a comment says, formatted as GitHub formats it: markdown and the HTML GitHub allows, its pictures fetched from
 * the host. A link, or a video, which shows as one, opens in the browser.
 */
@Composable
fun CommentBody(text: String, workbench: Workbench, modifier: Modifier = Modifier) {
    MarkdownView(text, modifier, images = workbench.comments.images, onLinkClick = { workbench.links.open(it) })
}
