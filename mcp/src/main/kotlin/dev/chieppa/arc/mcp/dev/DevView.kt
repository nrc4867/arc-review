package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.view.ViewNode

/** A view of a plugin Arc shows now, as [id]: its [kind], the [contribution] it shows, and the tree it draws. */
data class DevView(val id: String, val kind: ViewKind, val contribution: String, val nodes: List<ViewNode>)
