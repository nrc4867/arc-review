package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.change.SourceSnapshot

/** Where each changed file of a source stands, given the marks on it and the replays of what was reviewed. */
object FileStates {
    /** Each file of [snapshot] by path, with its state under [marks] (by path) and [replays]. */
    fun of(snapshot: SourceSnapshot, marks: Map<String, FileMark>, replays: List<ReplayOutcome> = emptyList()): Map<String, FileReviewState> =
        snapshot.files.associate { file ->
            val mark = marks[file.path]
            val replay = mark?.reviewed?.let { reviewed -> replays.firstOrNull { it.reviewed == reviewed } }
            file.path to FileReviewState.of(mark, snapshot.fingerprints[file.path], replay)
        }
}
