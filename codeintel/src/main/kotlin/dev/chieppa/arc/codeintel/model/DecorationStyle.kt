package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * The themed looks a decoration can take; the viewer picks the colours. [Reference] and [Definition] wash the text,
 * as a name's uses and its definition are marked; [Accent] underlines it dotted; [Warning] and [Error] underline it
 * wavy and mark the ruler.
 */
@Serializable
enum class DecorationStyle { Reference, Definition, Accent, Warning, Error }
