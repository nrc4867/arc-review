package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.start.StartContribution
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.ui.workbench.Workbench

/** Arc's own start cards and buttons for the sources [context] names, given through the start registry like any contributor's. */
fun arcStartProvider(workbench: Workbench): StartProvider = { context ->
    val sources = workbench.state.workspace.sources
    val selection = context.sources.mapNotNull { info -> sources.firstOrNull { it.ref == info.ref } }
    when (context.kind) {
        StartScreenKind.NothingOpen -> nothingOpenCards(workbench)
        StartScreenKind.OneSource -> selection.singleOrNull()?.let { oneSourceCards(workbench, it) } ?: StartContribution()
        StartScreenKind.SeveralSources -> severalSourcesCards(workbench, selection)
    }
}
