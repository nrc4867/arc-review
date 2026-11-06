package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.source.RepoName

/** Thrown when a repo has no clone that a [RepoLocator] knows of. */
class UnknownRepoException(val repo: RepoName) : Exception("No clone of $repo")
