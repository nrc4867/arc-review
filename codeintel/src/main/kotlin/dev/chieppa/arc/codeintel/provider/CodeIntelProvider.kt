package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.DocumentSelector

/**
 * Something that answers questions about code. Every request and answer is plain serialisable data, so a provider can
 * as well live in another process, such as a plugin, as in Arc. [id] names it in answers' origins; [selector] says which
 * files it answers for; a provider of higher [rank] is asked first and its answers listed first.
 */
interface CodeIntelProvider {
    val id: String
    val selector: DocumentSelector
    val rank: Int get() = 0
}
