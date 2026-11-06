package dev.chieppa.arc.store.files

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.file.Path

/** How the store writes JSON: readable, defaults left out, and unknown keys skipped on reading. */
object StoreJson {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /** [path] read as [serializer]'s type, or null when it is missing or does not parse. */
    fun <T> load(path: Path, serializer: KSerializer<T>): T? {
        val text = AtomicFile.read(path) ?: return null
        return try {
            json.decodeFromString(serializer, text)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun <T> save(path: Path, serializer: KSerializer<T>, value: T) {
        AtomicFile.write(path, json.encodeToString(serializer, value))
    }
}
