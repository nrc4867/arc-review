package dev.chieppa.arc.codeintel.provider

import dev.chieppa.arc.codeintel.model.CodeDecoration
import dev.chieppa.arc.codeintel.model.DecorationRequest

/** Adds decorations to code in the diff and full-file viewers. */
interface DecorationProvider : CodeIntelProvider {
    suspend fun decorations(request: DecorationRequest): List<CodeDecoration>
}
