package dev.chieppa.arc.host.github.history

import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.pulls.CheckRollups
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.SearchDocument
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonObject

/** The GraphQL document for the overall checks of several commits of one repo, each under an alias, and reading its answer. */
object CommitChecksQuery {
    private const val ALIAS = "c"

    fun text(repo: GitHubRepo, oids: List<String>): String {
        val fields = oids.withIndex().joinToString(" ") { (i, oid) ->
            "$ALIAS$i: object(oid: ${SearchDocument.quote(oid)}) { ... on Commit { statusCheckRollup { state } } }"
        }
        return "query { repository(owner: ${SearchDocument.quote(repo.owner)}, name: ${SearchDocument.quote(repo.name)}) { $fields } }"
    }

    /** Each of [oids] that has checks, with their overall state. */
    fun parse(oids: List<String>, data: JsonObject): Map<String, CheckState> {
        val repository = data.obj("repository")
        return oids.withIndex().mapNotNull { (i, oid) ->
            val state = CheckRollups.state(repository.obj("$ALIAS$i").obj("statusCheckRollup").string("state"))
            if (state == CheckState.None) null else oid to state
        }.toMap()
    }
}
