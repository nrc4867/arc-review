package dev.chieppa.arc.tools

import java.io.Closeable
import java.io.EOFException
import java.io.FilterInputStream
import java.io.InputStream

/**
 * Reads a tar stream entry by entry: ustar names with their prefix, pax `path` and `linkpath` records and GNU long
 * names and link names. Each entry's [content] must be read, or skipped by the next call to [next].
 */
class TarReader(private val input: InputStream) : Closeable {
    /**
     * One entry: its [name], what it is, and whether its owner may run it. A link names its [link] target, a path
     * relative to the link's folder for a [symbolic] one, relative to the archive's root for a hard one.
     */
    data class Entry(
        val name: String,
        val file: Boolean,
        val directory: Boolean,
        val executable: Boolean,
        val size: Long,
        val link: String? = null,
        val symbolic: Boolean = false,
    )

    private var remaining = 0L
    private var padding = 0L

    /** The next entry, or null at the end of the archive. */
    fun next(): Entry? {
        skip(remaining + padding)
        var longName: String? = null
        var longLink: String? = null
        while (true) {
            val header = readBlock() ?: return null
            if (header.all { it == ZERO }) return null
            val size = octal(header, SIZE_AT, SIZE_LENGTH)
            val type = header[TYPE_AT].toInt().toChar()
            if (type == PAX || type == GNU_LONG_NAME || type == GNU_LONG_LINK) {
                val body = readExactly(size)
                skip(pad(size))
                when (type) {
                    PAX -> {
                        longName = paxValue(body, PAX_PATH) ?: longName
                        longLink = paxValue(body, PAX_LINK_PATH) ?: longLink
                    }
                    GNU_LONG_NAME -> longName = body.toString(Charsets.UTF_8).trimEnd(NUL)
                    else -> longLink = body.toString(Charsets.UTF_8).trimEnd(NUL)
                }
                continue
            }
            if (type == PAX_GLOBAL) {
                skip(size + pad(size))
                continue
            }
            val prefix = text(header, PREFIX_AT, PREFIX_LENGTH)
            val name = longName ?: listOf(prefix, text(header, NAME_AT, NAME_LENGTH)).filter { it.isNotEmpty() }.joinToString(SLASH)
            val mode = octal(header, MODE_AT, MODE_LENGTH)
            remaining = size
            padding = pad(size)
            val file = type == REGULAR || type == NUL
            val linked = type == SYMBOLIC_LINK || type == HARD_LINK
            val link = if (linked) longLink ?: text(header, LINK_AT, LINK_LENGTH) else null
            return Entry(name, file, type == DIRECTORY, mode and OWNER_EXECUTE != 0L, size, link, type == SYMBOLIC_LINK)
        }
    }

    /** The current entry's bytes. */
    fun content(): InputStream = object : FilterInputStream(input) {
        override fun read(): Int {
            if (remaining <= 0) return -1
            return super.read().also { if (it >= 0) remaining-- }
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (remaining <= 0) return -1
            return super.read(b, off, minOf(len.toLong(), remaining).toInt()).also { if (it > 0) remaining -= it }
        }

        override fun close() = Unit
    }

    override fun close() = input.close()

    private fun readBlock(): ByteArray? {
        val block = ByteArray(BLOCK)
        var read = 0
        while (read < BLOCK) {
            val count = input.read(block, read, BLOCK - read)
            if (count < 0) return if (read == 0) null else throw EOFException()
            read += count
        }
        return block
    }

    private fun readExactly(size: Long): ByteArray = input.readNBytes(size.toInt()).also { if (it.size.toLong() != size) throw EOFException() }

    private fun skip(count: Long) {
        var left = count
        while (left > 0) {
            val skipped = input.skip(left)
            if (skipped <= 0) {
                if (input.read() < 0) return
                left--
            } else {
                left -= skipped
            }
        }
        remaining = 0
        padding = 0
    }

    private fun pad(size: Long): Long = (BLOCK - size % BLOCK) % BLOCK

    private fun paxValue(body: ByteArray, key: String): String? = body.toString(Charsets.UTF_8).lines()
        .map { it.substringAfter(' ') }
        .firstOrNull { it.startsWith(key) }
        ?.removePrefix(key)

    private fun text(header: ByteArray, at: Int, length: Int): String =
        String(header, at, length, Charsets.UTF_8).substringBefore(NUL)

    private fun octal(header: ByteArray, at: Int, length: Int): Long =
        text(header, at, length).trim().takeIf { it.isNotEmpty() }?.toLong(OCTAL) ?: 0L

    private companion object {
        const val BLOCK = 512
        const val ZERO: Byte = 0
        const val NAME_AT = 0
        const val NAME_LENGTH = 100
        const val MODE_AT = 100
        const val MODE_LENGTH = 8
        const val SIZE_AT = 124
        const val SIZE_LENGTH = 12
        const val TYPE_AT = 156
        const val LINK_AT = 157
        const val LINK_LENGTH = 100
        const val PREFIX_AT = 345
        const val PREFIX_LENGTH = 155
        const val OCTAL = 8
        const val OWNER_EXECUTE = 0b001_000_000L
        const val REGULAR = '0'
        const val DIRECTORY = '5'
        const val HARD_LINK = '1'
        const val SYMBOLIC_LINK = '2'
        const val PAX = 'x'
        const val PAX_GLOBAL = 'g'
        const val GNU_LONG_NAME = 'L'
        const val GNU_LONG_LINK = 'K'
        const val NUL = '\u0000'
        const val SLASH = "/"
        const val PAX_PATH = "path="
        const val PAX_LINK_PATH = "linkpath="
    }
}
