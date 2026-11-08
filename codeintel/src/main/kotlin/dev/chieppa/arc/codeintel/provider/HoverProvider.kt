package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.arc.codeintel.model.HoverRequest

/** Adds text shown while the pointer rests on code; null for none. */
interface HoverProvider : CodeIntelProvider {
    suspend fun hover(request: HoverRequest): HoverAnswer?
}
