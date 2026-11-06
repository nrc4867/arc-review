package dev.chieppa.arc.store.repos

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Path

/**
 * The repos Arc knows, by name, with the folder of each one's clone, kept in a JSON file at [path]. Every review shares
 * them. A missing file, or an entry whose name is not a repo name, reads as nothing.
 */
class RepoRegistryFile(private val path: Path) {
    fun load(): Map<RepoName, Path> =
        StoreJson.load(path, StoredRepos.serializer())?.repos.orEmpty()
            .mapNotNull { entry -> RepoName.parse(entry.name)?.let { it to Path.of(entry.path) } }
            .toMap()

    fun save(repos: Map<RepoName, Path>) {
        StoreJson.save(path, StoredRepos.serializer(), StoredRepos(repos.map { (name, dir) -> StoredRepo(name.value, dir.toString()) }))
    }
}
