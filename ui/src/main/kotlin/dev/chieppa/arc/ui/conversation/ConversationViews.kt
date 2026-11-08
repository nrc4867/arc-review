package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * How the person reads a conversation: its [grouping], which items they folded to one line, and which long comments
 * they opened whole. Items and comments are named by their keys.
 */
@Stable
class ConversationViews {
    var grouping by mutableStateOf(ConversationGrouping.Timeline)
    var folded by mutableStateOf(emptySet<String>())
    var whole by mutableStateOf(emptySet<String>())

    fun isFolded(key: String): Boolean = key in folded

    fun toggleFold(key: String) {
        folded = if (key in folded) folded - key else folded + key
    }

    fun showsWhole(key: String): Boolean = key in whole

    fun toggleWhole(key: String) {
        whole = if (key in whole) whole - key else whole + key
    }
}
