package dev.chieppa.arc.codeintel.router

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.codeintel.model.CodeDecoration
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DecorationRequest
import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.arc.codeintel.model.HoverRequest
import dev.chieppa.arc.codeintel.model.OutlineAnswer
import dev.chieppa.arc.codeintel.model.OutlineRequest
import dev.chieppa.arc.codeintel.model.UsagesAnswer
import dev.chieppa.arc.codeintel.model.UsagesRequest

/**
 * Asks the providers registered in [intel] that take a file and merges what they say with [AnswerMerging]. Every
 * provider of a kind is asked at once; one that fails is left out.
 */
class CodeIntelRouter(private val intel: CodeIntel) {
    suspend fun definitions(request: DefinitionRequest): DefinitionAnswer? =
        AnswerMerging.definitions(askAll(intel.definitions.matching(request.document)) { it.definitions(request) })

    suspend fun usages(request: UsagesRequest): UsagesAnswer? =
        AnswerMerging.usages(askAll(intel.usages.matching(request.document)) { it.usages(request) }, request.sources)

    /** What a Ctrl+click at [request] leads to: a jump, a list to choose from, the usages, or nothing. */
    suspend fun navigate(request: DefinitionRequest): Navigation {
        val answer = definitions(request) ?: return Navigation.Nothing
        if (answer.atDefinition) {
            val usages = usages(UsagesRequest(request.document, request.at, request.sources))
            return if (usages == null || usages.count == 0) Navigation.Nothing else Navigation.Usages(usages)
        }
        val confident = answer.targets.filter { it.confidence == Confidence.Confident }
        return when {
            answer.targets.isEmpty() -> Navigation.Nothing
            confident.size == 1 -> Navigation.Jump(confident.single())
            else -> Navigation.Choose(answer)
        }
    }

    suspend fun decorations(request: DecorationRequest): List<CodeDecoration> =
        askAll(intel.decorations.matching(request.document)) { it.decorations(request) }.flatten()

    suspend fun hovers(request: HoverRequest): List<HoverAnswer> =
        askAll(intel.hovers.matching(request.document)) { it.hover(request) }

    /** The outline from the highest-ranked provider that can read the file. */
    suspend fun outline(request: OutlineRequest): OutlineAnswer? =
        askAll(intel.outlines.matching(request.document)) { it.outline(request) }.firstOrNull()
}
