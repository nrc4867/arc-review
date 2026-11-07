package dev.chieppa.arc.ui.workbench

/**
 * A review the top bar's selector offers, by id and name, with its sources' refs; a null [id] stands for making a new
 * one.
 */
data class ReviewChoice(val id: String?, val name: String, val sources: Set<String> = emptySet())
