package dev.chieppa.arc.git.process

import dev.chieppa.arc.core.change.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedInputStream
import java.io.Closeable
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path

/**
 * One long-running `git cat-file --batch` in a repo, answering reads one at a time. It starts on the first read. A
 * read that is cancelled or fails ends the process, and the next read starts a new one, so a half-read answer never
 * reaches another caller.
 */
class CatFileBatch(private val dir: Path, private val runner: GitRunner = GitRunner()) : Closeable {
    private val lock = Mutex()
    private var process: Process? = null
    private var input: BufferedInputStream? = null
    private var output: OutputStream? = null

    /** The object [name] names, such as an object id or `rev:path`, or null when there is none. */
    suspend fun read(name: String): GitObject? {
        require('\n' !in name) { "An object name is one line" }
        return lock.withLock {
            runInterruptible(Dispatchers.IO) {
                try {
                    request(name)
                } catch (e: IOException) {
                    stop()
                    throw e
                } catch (e: InterruptedException) {
                    stop()
                    throw e
                }
            }
        }
    }

    /**
     * Reads every object of [names] in one stream, writing the requests from another thread while the answers are
     * read, and hands each to [consume] in order; null for a name that names no object.
     */
    suspend fun readAll(names: List<String>, consume: (name: String, GitObject?) -> Unit) {
        require(names.none { '\n' in it }) { "An object name is one line" }
        if (names.isEmpty()) return
        lock.withLock {
            runInterruptible(Dispatchers.IO) {
                try {
                    stream(names, consume)
                } catch (e: IOException) {
                    stop()
                    throw e
                } catch (e: InterruptedException) {
                    stop()
                    throw e
                }
            }
        }
    }

    private fun stream(names: List<String>, consume: (String, GitObject?) -> Unit) {
        if (process?.isAlive != true) begin()
        val out = output!!
        val reader = input!!
        val writer = Thread {
            try {
                for (name in names) out.write((name + "\n").toByteArray(Charsets.UTF_8))
                out.flush()
            } catch (_: IOException) {
                // The reading side sees the process end and fails the read.
            }
        }.apply {
            isDaemon = true
            start()
        }
        try {
            for (name in names) consume(name, answer(reader))
        } finally {
            writer.join()
        }
    }

    private fun request(name: String): GitObject? {
        if (process?.isAlive != true) begin()
        val out = output!!
        val reader = input!!
        out.write((name + "\n").toByteArray(Charsets.UTF_8))
        out.flush()
        return answer(reader)
    }

    private fun answer(reader: BufferedInputStream): GitObject? {
        val header = readLine(reader)
        val parts = header.split(' ')
        if (parts.size != HEADER_PARTS) return null
        val size = parts[2].toInt()
        val bytes = reader.readNBytes(size)
        if (bytes.size != size || reader.read() != '\n'.code) throw IOException("cat-file ended mid-object")
        return GitObject(ObjectId(parts[0]), parts[1], bytes)
    }

    private fun readLine(stream: BufferedInputStream): String {
        val line = StringBuilder()
        while (true) {
            val b = stream.read()
            if (b < 0) throw IOException("cat-file ended")
            if (b == '\n'.code) return line.toString()
            line.append(b.toChar())
        }
    }

    private fun begin() {
        stop()
        val started = runner.start(dir, listOf("cat-file", "--batch"), discardErrors = true)
        process = started
        input = BufferedInputStream(started.inputStream)
        output = started.outputStream
    }

    private fun stop() {
        process?.destroyForcibly()
        process = null
        input = null
        output = null
    }

    override fun close() {
        stop()
    }

    private companion object {
        const val HEADER_PARTS = 3
    }
}
