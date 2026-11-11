package dev.chieppa.arc.plugins.starter

/** Why a starter plugin could not be made. */
enum class StarterProblem {
    /** The name holds no letter or digit, or a character a plugin's name may not hold. */
    BadName,

    /** The folder it would go in holds files already. */
    FolderTaken,

    /** A file could not be written. */
    NotWritten,
}
