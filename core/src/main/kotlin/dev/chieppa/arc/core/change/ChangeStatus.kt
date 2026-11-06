package dev.chieppa.arc.core.change

/** How a file changed between two revisions, with the letter the tree shows for it. */
enum class ChangeStatus(val letter: Char) {
    Added('A'),
    Modified('M'),
    Deleted('D'),
    Renamed('R'),
    Copied('C'),

    /** The file became or stopped being a symlink or submodule. */
    TypeChanged('T'),
    ;

    companion object {
        /** The status git's raw diff writes as [letter], such as `M` or `R` in `R087`. */
        fun ofGitLetter(letter: Char): ChangeStatus? = entries.firstOrNull { it.letter == letter }
    }
}
