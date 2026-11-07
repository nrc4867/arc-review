package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Whether the switcher is open, what its field holds, the [scope] it searches and the [index] of the row Enter picks. */
class SwitcherState {
    var open by mutableStateOf(false)
        private set
    var query by mutableStateOf("")
        private set
    var scope by mutableStateOf(SwitcherScope.All)
        private set
    var index by mutableIntStateOf(0)

    /** Opens the switcher on [scope] with an empty field, or with [query] in it. */
    fun show(scope: SwitcherScope = SwitcherScope.All, query: String = "") {
        this.scope = scope
        this.query = query
        index = 0
        open = true
    }

    fun close() {
        open = false
    }

    fun type(text: String) {
        query = text
        index = 0
    }

    fun pick(scope: SwitcherScope) {
        this.scope = scope
        index = 0
    }

    /** Moves the highlighted row by [step] among [rows] rows. */
    fun move(step: Int, rows: Int) {
        index = (index + step).coerceIn(0, (rows - 1).coerceAtLeast(0))
    }
}
