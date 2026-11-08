package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.conversation.PullConversation

/** A pull request's conversation as its host holds it, with the host's id for the pull request, which writes need. */
data class HostedConversation(val pullId: String, val conversation: PullConversation)
