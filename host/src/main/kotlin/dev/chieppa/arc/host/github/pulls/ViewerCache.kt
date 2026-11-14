package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.tools.Sha256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant

/**
 * Who the person is, by the token they use: kept in memory and in [file] under the token's SHA-256 (never the token
 * itself), read again from GitHub once it is [ttl] old or the token changes. When GitHub cannot answer, the person last
 * read with the same token stands.
 */
class ViewerCache(
    private val file: ViewerFile? = null,
    private val ttl: Duration = TTL,
    private val clock: () -> Instant = Instant::now,
) {
    private val lock = Mutex()

    @Volatile
    private var held: ViewerRecord? = null

    @Volatile
    private var loaded = false

    /** The person [token] belongs to, from here while fresh, else through [fetch]. */
    suspend fun viewer(token: String, fetch: suspend () -> Viewer): Viewer = lock.withLock {
        val key = keyOf(token)
        val stored = stored(key)
        if (stored != null && Duration.between(stored.at, clock()) < ttl) return stored.viewer
        val fetched = try {
            fetch()
        } catch (e: GitHubException) {
            if (stored != null && e.reason != GitHubException.Reason.Unauthorized) return stored.viewer
            throw e
        }
        val record = ViewerRecord(key, fetched, clock())
        held = record
        file?.let { withContext(Dispatchers.IO) { it.write(record) } }
        fetched
    }

    /** The person last read with [token], however old, without asking GitHub; null when none was. */
    suspend fun known(token: String): Viewer? = lock.withLock { stored(keyOf(token))?.viewer }

    private suspend fun stored(key: String): ViewerRecord? {
        if (!loaded) {
            loaded = true
            held = file?.let { withContext(Dispatchers.IO) { it.read() } }
        }
        return held?.takeIf { it.key == key }
    }

    private fun keyOf(token: String): String = Sha256.hex(MessageDigest.getInstance(Sha256.ALGORITHM).digest(token.toByteArray()))

    companion object {
        /** How long who the person is stands before GitHub is asked again. */
        val TTL: Duration = Duration.ofHours(24)
    }
}
