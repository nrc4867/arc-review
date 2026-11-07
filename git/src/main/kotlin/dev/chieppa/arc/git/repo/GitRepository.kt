package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.parse.CommitLogParser
import dev.chieppa.arc.git.parse.DiffFileOutput
import dev.chieppa.arc.git.parse.DiffOutputParser
import dev.chieppa.arc.git.parse.NulReader
import dev.chieppa.arc.git.process.CatFileBatch
import dev.chieppa.arc.git.process.GitResult
import dev.chieppa.arc.git.process.GitRunner
import dev.chieppa.arc.git.process.GitSettings
import java.io.Closeable
import java.nio.file.Path

/**
 * One repo's git data, read through the git CLI: whole-change operations run as one process each, and files are read
 * through one long-running `cat-file --batch`. Nothing is checked out. Every call runs off the calling thread and ends
 * its process when cancelled.
 */
class GitRepository(val dir: Path, private val runner: GitRunner = GitRunner()) : Closeable {
    private val objects = CatFileBatch(dir, runner)

    suspend fun git(args: List<String>, input: ByteArray? = null, accept: Set<Int> = setOf(0)): GitResult =
        runner.run(dir, args, input, accept)

    /** The commit [rev] names, such as a sha, a branch or `HEAD~2`, or null when it names none. */
    suspend fun resolveCommit(rev: String): ObjectId? {
        val result = git(listOf("rev-parse", "--verify", "--quiet", "--end-of-options", "$rev^{commit}"), accept = setOf(0, GitSettings.EXIT_SOME, FATAL))
        return result.text.trim().takeIf { result.exitCode == 0 && it.isNotEmpty() }?.let(::ObjectId)
    }

    /** Whether [ancestor] is [commit] or one of its ancestors. */
    suspend fun isAncestor(ancestor: ObjectId, commit: ObjectId): Boolean =
        git(listOf("merge-base", "--is-ancestor", ancestor.value, commit.value), accept = setOf(0, GitSettings.EXIT_SOME)).exitCode == 0

    /** The first parent of [commit], or null for a root commit. */
    suspend fun firstParent(commit: ObjectId): ObjectId? = resolveCommit("$commit^1")

    /** The best common ancestor of [a] and [b], or null when they share none. */
    suspend fun mergeBase(a: ObjectId, b: ObjectId): ObjectId? {
        val result = git(listOf("merge-base", a.value, b.value), accept = setOf(0, GitSettings.EXIT_SOME))
        return result.text.trim().takeIf { result.exitCode == 0 && it.isNotEmpty() }?.let(::ObjectId)
    }

    /** The branch the repo's remote names as its default, else `main` or `master` if one exists. */
    suspend fun defaultBranch(): String? {
        val remote = git(listOf("symbolic-ref", "--quiet", "--short", REMOTE_HEAD), accept = setOf(0, GitSettings.EXIT_SOME, FATAL))
        remote.text.trim().takeIf { remote.exitCode == 0 && it.isNotEmpty() }?.let { return it }
        return DEFAULT_BRANCHES.firstOrNull { resolveCommit(it) != null }
    }

    /**
     * Every changed file from [base] to [head] with its line counts, blobs and `-U0` patch section, in one process.
     * The patch is made with [GitSettings.DIFF_OPTIONS], so fingerprints taken from it do not depend on config.
     */
    suspend fun diff(base: ObjectId, head: ObjectId): List<DiffFileOutput> {
        val args = listOf("diff") + GitSettings.DIFF_OPTIONS + listOf("-U0", "--raw", "--numstat", "-z", "-p", base.value, head.value, "--")
        return DiffOutputParser.parse(git(args).stdout)
    }

    /** The commits after [base] up to [head], oldest first. */
    suspend fun commits(base: ObjectId?, head: ObjectId): List<CommitInfo> {
        val range = if (base == null) listOf(head.value) else listOf("${base.value}..${head.value}")
        return CommitLogParser.parse(git(listOf("log", "--reverse", "--no-color", CommitLogParser.FORMAT) + range + "--").text)
    }

    /** [commit] as a list of commits shows it. */
    suspend fun commit(commit: ObjectId): CommitInfo? =
        CommitLogParser.parse(git(listOf("log", "-1", "--no-color", CommitLogParser.FORMAT, commit.value, "--")).text).firstOrNull()

    /** Every file path in [rev]'s tree, changed or not, in git's order. */
    suspend fun listFiles(rev: String): List<String> {
        val reader = NulReader(git(listOf("ls-tree", "-r", "-z", "--name-only", "--full-tree", rev)).stdout)
        return buildList { while (!reader.atEnd) reader.field().takeIf { it.isNotEmpty() }?.let(::add) }
    }

    /** The paths whose entry differs between trees [a] and [b], renames not followed. */
    suspend fun differingPaths(a: ObjectId, b: ObjectId): Set<String> {
        val reader = NulReader(git(listOf("diff-tree", "-r", "-z", "--name-only", "--no-renames", a.value, b.value)).stdout)
        return buildSet { while (!reader.atEnd) reader.field().takeIf { it.isNotEmpty() }?.let(::add) }
    }

    /** The bytes of blob [id], or null when the repo does not have it. */
    suspend fun readBlob(id: ObjectId): ByteArray? = objects.read(id.value)?.takeIf { it.type == BLOB }?.bytes

    /** The bytes of [path] at [rev], or null when there is no such file. */
    suspend fun readFile(rev: String, path: String): ByteArray? = objects.read("$rev:$path")?.takeIf { it.type == BLOB }?.bytes

    /** The URL of the remote [name], or null when there is no such remote. */
    suspend fun remoteUrl(name: String = REMOTE): String? {
        val result = git(listOf("config", "--get", "remote.$name.url"), accept = setOf(0, GitSettings.EXIT_SOME))
        return result.text.trim().takeIf { result.exitCode == 0 && it.isNotEmpty() }
    }

    /** Fetches [refspecs] from the remote [remote], without tags and without touching `FETCH_HEAD`. */
    suspend fun fetch(refspecs: List<String>, remote: String = REMOTE) {
        git(listOf("fetch", "--quiet", "--no-tags", "--no-write-fetch-head", "--recurse-submodules=no", remote) + refspecs)
    }

    /** Points [ref] at [commit], creating it if need be. */
    suspend fun updateRef(ref: String, commit: ObjectId) {
        git(listOf("update-ref", ref, commit.value))
    }

    /** Points each ref of [refs] at its commit, in one process. */
    suspend fun updateRefs(refs: Map<String, ObjectId>) {
        if (refs.isEmpty()) return
        val input = refs.entries.joinToString("") { (ref, commit) -> "update $ref ${commit.value}\n" }
        git(listOf("update-ref", "--stdin"), input.toByteArray())
    }

    /** Whether the repo is a partial clone, whose blobs arrive from its remote when fetched. */
    suspend fun isPartial(): Boolean {
        val result = git(listOf("config", "--get", "remote.origin.promisor"), accept = setOf(0, GitSettings.EXIT_SOME))
        return result.text.trim() == "true"
    }

    override fun close() {
        objects.close()
    }

    private companion object {
        const val FATAL = 128
        const val BLOB = "blob"
        const val REMOTE = "origin"
        const val REMOTE_HEAD = "refs/remotes/origin/HEAD"
        val DEFAULT_BRANCHES = listOf("main", "master")
    }
}
