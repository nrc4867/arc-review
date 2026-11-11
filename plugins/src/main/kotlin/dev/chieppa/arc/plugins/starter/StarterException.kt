package dev.chieppa.arc.plugins.starter

/** A starter plugin could not be made, for [problem]; [detail] names the name, folder or failure. */
class StarterException(val problem: StarterProblem, val detail: String) : Exception("$problem: $detail")
