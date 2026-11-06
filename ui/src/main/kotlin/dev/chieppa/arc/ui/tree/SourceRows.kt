package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.ReviewProgress
import dev.chieppa.arc.core.source.Source

/** What the tree needs to know of one source: what it is called, its files as shown, their states and its progress. */
data class SourceRows(
    val source: Source,
    val title: String,
    val files: List<ChangedFile>?,
    val states: Map<String, FileReviewState>,
    val progress: ReviewProgress?,
    val failed: Boolean = false,
)
