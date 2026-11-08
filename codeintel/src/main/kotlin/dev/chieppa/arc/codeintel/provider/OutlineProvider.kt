package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.OutlineAnswer
import dev.chieppa.arc.codeintel.model.OutlineRequest

/** Answers a file's outline; null when it cannot read the file. */
interface OutlineProvider : CodeIntelProvider {
    suspend fun outline(request: OutlineRequest): OutlineAnswer?
}
