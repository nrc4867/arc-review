package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest

/** Answers where a clicked name is defined; null when it has nothing to say about that file or name. */
interface DefinitionProvider : CodeIntelProvider {
    suspend fun definitions(request: DefinitionRequest): DefinitionAnswer?
}
