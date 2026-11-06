package dev.chieppa.arc.git.parse

/** Reads NUL- and tab-separated fields from git's `-z` output, from the front. */
internal class NulReader(private val bytes: ByteArray, var position: Int = 0) {
    val atEnd: Boolean get() = position >= bytes.size

    /** The next byte, without moving past it, or -1 at the end. */
    fun peek(): Int = if (atEnd) -1 else bytes[position].toInt() and BYTE_MASK

    /** The text up to the next [stop] byte, moving past the stop. */
    fun until(stop: Byte): String {
        val start = position
        while (position < bytes.size && bytes[position] != stop) position++
        val text = String(bytes, start, position - start, Charsets.UTF_8)
        if (position < bytes.size) position++
        return text
    }

    fun field(): String = until(NUL)

    fun skip(byte: Byte) {
        if (!atEnd && bytes[position] == byte) position++
    }

    /** Everything from here to the end. */
    fun rest(): ByteArray = bytes.copyOfRange(position.coerceAtMost(bytes.size), bytes.size)

    companion object {
        const val NUL: Byte = 0
        const val TAB: Byte = '\t'.code.toByte()
        private const val BYTE_MASK = 0xff
    }
}
