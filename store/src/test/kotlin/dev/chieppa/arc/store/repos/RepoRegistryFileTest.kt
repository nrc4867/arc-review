package dev.chieppa.arc.store.repos

import dev.chieppa.arc.core.source.RepoName
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class RepoRegistryFileTest {
    private val file = Files.createTempDirectory("arc-repos").resolve("repos.json")

    @Test
    fun reposAndTheirFoldersSurviveARoundTrip() {
        val repos = mapOf(RepoName("stretch") to Path.of("C:/code/stretch"), RepoName("squash") to Path.of("/home/a/squash"))
        RepoRegistryFile(file).save(repos)
        assertEquals(repos, RepoRegistryFile(file).load())
    }

    @Test
    fun aMissingFileHoldsNoRepos() {
        assertEquals(emptyMap(), RepoRegistryFile(file).load())
    }
}
