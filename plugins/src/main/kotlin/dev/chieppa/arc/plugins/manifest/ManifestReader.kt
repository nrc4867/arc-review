package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Reads `plugin.json` and `plugins.json` text, saying what is wrong with one that cannot be used. */
object ManifestReader {
    private val json = Json { ignoreUnknownKeys = true }
    private val ID = Regex("[a-z0-9][a-z0-9._-]{0,63}")
    private const val BAD_ID = "Plugin ids are lowercase letters, digits, dots, dashes and underscores:"
    private const val BAD_API = "Unsupported plugin API version"
    private const val NO_ENTRY = "No entry file"

    /** The manifest in [text], or why it cannot be used: not JSON of the right shape, a bad id or an API version Arc lacks. */
    fun plugin(text: String): ManifestResult<PluginManifest> {
        val read = decode(PluginManifest.serializer(), text)
        val manifest = (read as? ManifestResult.Read)?.value ?: return read
        return when {
            !ID.matches(manifest.id) -> ManifestResult.Invalid("$BAD_ID ${manifest.id}")
            manifest.apiVersion !in ApiVersion.SUPPORTED -> ManifestResult.Invalid("$BAD_API ${manifest.apiVersion}")
            manifest.entry.isBlank() -> ManifestResult.Invalid(NO_ENTRY)
            else -> read
        }
    }

    /** The list in [text], or why it cannot be used. */
    fun list(text: String): ManifestResult<PluginsList> = decode(PluginsList.serializer(), text)

    private fun <T> decode(serializer: KSerializer<T>, text: String): ManifestResult<T> = try {
        ManifestResult.Read(json.decodeFromString(serializer, text))
    } catch (e: SerializationException) {
        ManifestResult.Invalid(e.message ?: e.toString())
    } catch (e: IllegalArgumentException) {
        ManifestResult.Invalid(e.message ?: e.toString())
    }
}
