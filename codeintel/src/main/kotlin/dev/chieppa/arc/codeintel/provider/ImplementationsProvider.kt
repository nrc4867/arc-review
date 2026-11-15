package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.ImplementationsAnswer
import dev.chieppa.arc.codeintel.model.ImplementationsRequest

/** Answers what implements or overrides the type or member at a place; null when it has nothing to say. */
interface ImplementationsProvider : CodeIntelProvider {
    suspend fun implementations(request: ImplementationsRequest): ImplementationsAnswer?
}
