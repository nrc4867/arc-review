package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.ui.workbench.Workbench

/** Arc's own start cards and buttons, given through the start registry like any contributor's. */
fun arcStartProvider(workbench: Workbench): StartProvider = { context ->
    val selection = workbench.state.selection
    when (context.kind) {
        StartScreenKind.NothingOpen -> nothingOpenCards(workbench)
        StartScreenKind.OneSource -> oneSourceCards(workbench, selection.single())
        StartScreenKind.SeveralSources -> severalSourcesCards(workbench, selection)
    }
}
