package dev.chieppa.arc.host.github.avatars

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvatarCacheTest {
    private val url = "https://avatars.githubusercontent.com/u/2?u=abc&v=4"
    private val sized = "https://avatars.githubusercontent.com/u/2?u=abc&v=4&s=64"
    private var now = Instant.parse("2026-11-08T12:00:00Z")
    private val folder: Path = Files.createTempDirectory("avatars")

    private class CountingFetcher(var answer: ByteArray?) : AvatarFetcher {
        val asked = mutableListOf<String>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun fetch(url: String): ByteArray? {
            asked += url
            gate?.await()
            return answer
        }
    }

    private fun cache(fetcher: AvatarFetcher) = AvatarCache(folder, fetcher, clock = { now })

    @Test
    fun avatarsAreAskedForAtArcsSize() {
        assertEquals(sized, AvatarUrls.sized(url))
        assertEquals("https://a.example/u/1?v=4&s=64", AvatarUrls.sized("https://a.example/u/1?s=460&v=4"))
        assertEquals("https://a.example/u/1?s=32", AvatarUrls.sized("https://a.example/u/1", 32))
    }

    @Test
    fun aFreshPictureComesFromTheCacheWithoutFetching() = runTest {
        val fetcher = CountingFetcher(byteArrayOf(1, 2))
        val cache = cache(fetcher)
        assertContentEquals(byteArrayOf(1, 2), cache.bytes(url))
        assertContentEquals(byteArrayOf(1, 2), cache.bytes(url))
        assertEquals(listOf(sized), fetcher.asked)
        assertContentEquals(byteArrayOf(1, 2), Files.readAllBytes(folder.resolve(AvatarUrls.fileName(sized))))

        val restarted = CountingFetcher(byteArrayOf(9))
        assertContentEquals(byteArrayOf(1, 2), cache(restarted).bytes(url))
        assertEquals(emptyList(), restarted.asked)
    }

    @Test
    fun aStalePictureIsFetchedAgain() = runTest {
        val fetcher = CountingFetcher(byteArrayOf(1))
        val cache = cache(fetcher)
        cache.bytes(url)
        now += AvatarCache.TTL + Duration.ofMinutes(1)
        fetcher.answer = byteArrayOf(2)
        assertContentEquals(byteArrayOf(2), cache.bytes(url))
        assertEquals(2, fetcher.asked.size)
    }

    @Test
    fun aFailedFetchKeepsTheStaleCopy() = runTest {
        cache(CountingFetcher(byteArrayOf(5))).bytes(url)
        now += AvatarCache.TTL.multipliedBy(2)
        val failing = CountingFetcher(null)
        assertContentEquals(byteArrayOf(5), cache(failing).bytes(url))
        assertEquals(1, failing.asked.size)
        assertNull(cache(failing).bytes("https://a.example/u/404"))
    }

    @Test
    fun requestsAtOnceShareOneFetch() = runTest {
        val fetcher = CountingFetcher(byteArrayOf(7)).apply { gate = CompletableDeferred() }
        val cache = cache(fetcher)
        val requests = List(3) { async { cache.bytes(url) } }
        testScheduler.advanceUntilIdle()
        fetcher.gate!!.complete(Unit)
        requests.awaitAll().forEach { assertContentEquals(byteArrayOf(7), it) }
        assertEquals(1, fetcher.asked.size)
    }
}
