package dev.chieppa.arc.git.source

/** The refs a pull request is read through: GitHub's on `origin`, and Arc's private ones in the clone. */
object PullRequestRefNames {
    private const val PRIVATE = "refs/arc/pull/"
    private const val HEADS = "refs/heads/"
    private const val PULL = "refs/pull/"
    private const val HEAD = "/head"
    private const val BASE = "/base"

    /** Where Arc keeps pull request [number]'s head. */
    fun head(number: Int): String = "$PRIVATE$number$HEAD"

    /** Where Arc keeps the tip of pull request [number]'s base branch. */
    fun base(number: Int): String = "$PRIVATE$number$BASE"

    /** GitHub's ref for pull request [number]'s head. */
    fun pullHead(number: Int): String = "$PULL$number$HEAD"

    fun branch(name: String): String = HEADS + name
}
