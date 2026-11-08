package dev.chieppa.arc.core.start

/** What a start screen contributor is asked about: which screen shows, and the selected [sources] in strip order. */
data class StartContext(val kind: StartScreenKind, val sources: List<StartSourceInfo> = emptyList())
