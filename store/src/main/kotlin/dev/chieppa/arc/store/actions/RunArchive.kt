package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.store.files.StoreJson
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * The runs the app keeps, one JSON file each under [folder] at `<owner>/<name>/<id>.json`: the run as last read, its
 * jobs and the kept part of their logs. A run is kept while it is in any filter and for [KEEP_MILLIS] after.
 */
class RunArchive(private val folder: Path) {
    fun save(record: RunRecord) {
        StoreJson.save(file(record.run.ref), StoredRunRecord.serializer(), RunRecordMapping.toStored(record))
    }

    fun load(ref: RunRef): RunRecord? = StoreJson.load(file(ref), StoredRunRecord.serializer())?.let(RunRecordMapping::toRecord)

    /** Deletes the runs not in [keep] that were last seen in a filter before [now] less [KEEP_MILLIS]. */
    fun prune(keep: Set<RunRef>, now: Long) {
        if (!Files.isDirectory(folder)) return
        val files = try {
            Files.walk(folder).use { paths -> paths.filter { it.isRegularFile() && it.name.endsWith(JSON) }.toList() }
        } catch (_: IOException) {
            return
        }
        for (path in files) {
            val relative = folder.relativize(path)
            if (relative.nameCount != 3) continue
            val ref = path.nameWithoutExtension.toLongOrNull()?.let { RunRef("${relative.getName(0)}/${relative.getName(1)}", it) } ?: continue
            if (keep.any { it.repo.equals(ref.repo, true) && it.id == ref.id }) continue
            val seen = StoreJson.load(path, StoredRunRecord.serializer())?.seenAt ?: 0
            if (seen < now - KEEP_MILLIS) Files.deleteIfExists(path)
        }
    }

    private fun file(ref: RunRef): Path {
        val (owner, name) = ref.repo.lowercase().split('/', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return folder.resolve(owner).resolve(name).resolve("${ref.id}$JSON")
    }

    companion object {
        const val KEEP_MILLIS = 30L * 24 * 60 * 60 * 1000
        private const val JSON = ".json"
    }
}
