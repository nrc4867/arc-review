package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.CoroutineScope

/**
 * Everything the window uses to read and write a review's conversation: the conversation itself, where the person's
 * comments sit as sources move, pushing, and the people's pictures.
 */
@Stable
class CommentTools(
    state: WorkspaceState,
    data: ReviewData,
    service: ConversationService,
    scope: CoroutineScope,
    dispatchers: ArcDispatchers,
    initial: LocalConversation = LocalConversation(),
) {
    val controller = ConversationController(service, scope, initial)
    val placer = StagedPlacer(data, dispatchers.default)
    val pusher = ReviewPusher(state, controller, placer, service, scope)
    val avatars = AvatarImages(service, scope, dispatchers.default)
}
