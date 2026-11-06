package dev.chieppa.arc.git.process

/** What a finished git command gave: its exit code and both outputs, standard output as raw bytes. */
class GitResult(val exitCode: Int, val stdout: ByteArray, val stderr: String) {
    /** Standard output as UTF-8 text. */
    val text: String get() = stdout.toString(Charsets.UTF_8)
}
