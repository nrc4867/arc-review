package dev.chieppa.arc.git.process

/** A git command that failed: its [args], its [exitCode] and what it wrote to standard error. */
class GitException(val args: List<String>, val exitCode: Int, val stderr: String) :
    Exception("git ${args.joinToString(" ")} exited with $exitCode: ${stderr.trim()}")
