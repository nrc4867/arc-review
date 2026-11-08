package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.UsagesAnswer
import dev.chieppa.arc.codeintel.model.UsagesRequest

/** Answers where a defined name is used; null when it has nothing to say. */
interface UsagesProvider : CodeIntelProvider {
    suspend fun usages(request: UsagesRequest): UsagesAnswer?
}
