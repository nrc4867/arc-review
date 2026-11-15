package dev.chieppa.arc.ui.workbench

import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.frame.capsFor

/** The key caps of action [id]'s binding, for a tooltip or menu; null when it is not registered or has no key. */
fun Workbench.keysFor(id: ActionId): List<String>? = registry[id]?.let(keymap::capsFor)
