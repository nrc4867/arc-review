package dev.chieppa.arc.codeintel.router

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.model.UsagesAnswer

/**
 * How answers from several providers become one. A place named twice keeps its most precise, most confident answer.
 * Where an exact provider is confident about a language, by-name answers in that language drop to filtered, and
 * by-name usage groups give way to exact ones for the same source and language.
 */
object AnswerMerging {
    /** Confident before filtered, exact before by name; otherwise the order given. */
    val targetOrder: Comparator<SymbolTarget> = compareBy<SymbolTarget> { it.confidence }.thenByDescending { it.origin.precision }

    fun definitions(answers: List<DefinitionAnswer>): DefinitionAnswer? {
        val first = answers.firstOrNull() ?: return null
        val targets = answers.flatMap { it.targets }
        val exactLanguages = targets.filter { it.origin.precision == Precision.Exact && it.confidence == Confidence.Confident }
            .map { it.location.document.language }.toSet()
        val demoted = targets.map { target ->
            val superseded = target.origin.precision == Precision.ByName && target.location.document.language in exactLanguages
            if (superseded) target.copy(confidence = Confidence.Filtered) else target
        }
        val best = demoted.groupBy { it.location }.values.map { same -> same.minWith(targetOrder) }
        return DefinitionAnswer(first.name, best.sortedWith(targetOrder), answers.any { it.atDefinition })
    }

    fun usages(answers: List<UsagesAnswer>, sources: List<SourceScope>): UsagesAnswer? {
        val first = answers.firstOrNull() ?: return null
        val groups = answers.flatMap { it.groups }.filter { it.usages.isNotEmpty() }
        val exact = groups.filter { it.origin.precision == Precision.Exact }.map { it.source to it.language }.toSet()
        val kept = groups.filterNot { it.origin.precision == Precision.ByName && (it.source to it.language) in exact }
        val order = sources.map { it.source }
        val sorted = kept.sortedBy { group -> order.indexOf(group.source).let { if (it < 0) order.size else it } }
        return UsagesAnswer(first.name, sorted)
    }

    /** Usages as a group lists them: files the change touches first, confident before filtered, then by place. */
    fun orderUsages(usages: List<SymbolTarget>, changedPaths: Collection<String>): List<SymbolTarget> {
        val changed = changedPaths.toSet()
        return usages.sortedWith(
            compareBy<SymbolTarget> { it.location.document.path !in changed }
                .thenBy { it.confidence }
                .thenBy { it.location.document.path }
                .thenBy { it.location.range.start },
        )
    }
}
