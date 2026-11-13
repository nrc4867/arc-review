package dev.chieppa.arc.host.github.releases

import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

/** GitHub's `GET /repos/{owner}/{repo}/releases` answer as [Release]s; entries missing a tag are left out. */
object ReleaseParser {
    fun parse(json: JsonElement): List<Release> = (json as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.let(::release) }

    /** The SHA-256 of each file `SHA256SUMS.txt` lists (`<hex> <name>`, the name maybe marked binary with `*`), by name. */
    fun sums(text: String): Map<String, String> = text.lines().mapNotNull { line ->
        val parts = line.trim().split(Regex("\\s+"), limit = 2)
        if (parts.size == 2 && parts[0].length == SHA256_HEX) parts[1].removePrefix(BINARY) to parts[0].lowercase() else null
    }.toMap()

    private fun release(item: JsonObject): Release? {
        val tag = item.string("tag_name") ?: return null
        return Release(
            tag = tag,
            version = tag.removePrefix(TAG_PREFIX),
            prerelease = item.boolean("prerelease") ?: false,
            draft = item.boolean("draft") ?: false,
            assets = item.objects("assets").mapNotNull(::asset),
        )
    }

    private fun asset(item: JsonObject): ReleaseAsset? = ReleaseAsset(
        name = item.string("name") ?: return null,
        size = (item["size"] as? JsonPrimitive)?.longOrNull ?: 0L,
        url = item.string("url") ?: return null,
    )

    private const val TAG_PREFIX = "v"
    private const val BINARY = "*"
    private const val SHA256_HEX = 64
}
