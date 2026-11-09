package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.artifacts.ArtifactKind
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.ui.services.ActionsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

/**
 * Opens a downloaded artifact: a zip's entries, or a single file, picking the entry worth showing first, a JUnit report,
 * Markdown, JSON, text, a picture or HTML in that order. Entries larger than [MAX_ENTRY] are not read.
 */
object ArtifactReader {
    const val MAX_ENTRY = 8L * 1024 * 1024
    private val ZIP = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
    private val ORDER = listOf(ArtifactKind.JUnit, ArtifactKind.Markdown, ArtifactKind.Json, ArtifactKind.Text, ArtifactKind.Image, ArtifactKind.Html)
    private const val HEAD = 256

    /** [artifact] of [run], downloaded when it is not cached; null when it cannot be had or holds nothing to show. */
    suspend fun read(service: ActionsService, run: RunRef, artifact: RunArtifact, io: CoroutineDispatcher, entry: String? = null): ArtifactContent? {
        val file = try {
            service.artifactFile(run, artifact)
        } catch (_: GitHubException) {
            null
        } ?: return null
        return withContext(io) { open(file, artifact.name, entry) }
    }

    /** What [file] holds, picking [entry] when it is given. */
    fun open(file: Path, name: String, entry: String? = null): ArtifactContent? = try {
        if (isZip(file)) zipEntry(file, entry) else single(file, name)
    } catch (_: IOException) {
        null
    }

    private fun single(file: Path, name: String): ArtifactContent? {
        if (Files.size(file) > MAX_ENTRY) return null
        val bytes = Files.readAllBytes(file)
        return ArtifactContent(name, ArtifactKind.of(name, bytes.copyOf(minOf(HEAD, bytes.size))), bytes, file, listOf(name))
    }

    private fun zipEntry(file: Path, wanted: String?): ArtifactContent? = ZipFile(file.toFile()).use { zip ->
        val entries = zip.entries().toList().filterNot { it.isDirectory }
        val kinds = entries.associateWith { entry ->
            val head = zip.getInputStream(entry).use { it.readNBytes(HEAD) }
            ArtifactKind.of(entry.name, head)
        }
        val pick = entries.firstOrNull { it.name == wanted }
            ?: ORDER.firstNotNullOfOrNull { kind -> entries.firstOrNull { kinds[it] == kind && it.size in 0..MAX_ENTRY } }
            ?: return@use null
        val bytes = zip.getInputStream(pick).use { it.readNBytes(MAX_ENTRY.toInt()) }
        ArtifactContent(pick.name, kinds.getValue(pick), bytes, file, entries.map { it.name })
    }

    /** Unpacks [file] into [folder] for opening in the browser, keeping every entry inside it; the HTML entry's path. */
    fun unpack(file: Path, folder: Path, entry: String): Path? = try {
        ZipFile(file.toFile()).use { zip ->
            val root = folder.toAbsolutePath().normalize()
            for (item in zip.entries()) {
                val target = root.resolve(item.name).normalize()
                if (!target.startsWith(root) || item.isDirectory) continue
                Files.createDirectories(target.parent)
                zip.getInputStream(item).use { Files.copy(it, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
            }
            root.resolve(entry).normalize().takeIf { it.startsWith(root) }
        }
    } catch (_: IOException) {
        null
    }

    private fun isZip(file: Path): Boolean = Files.newInputStream(file).use { it.readNBytes(ZIP.size) }.contentEquals(ZIP)
}
