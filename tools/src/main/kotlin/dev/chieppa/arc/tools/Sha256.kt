package dev.chieppa.arc.tools

import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest

/** SHA-256 digests as lowercase hex, the way pins and `SHA256SUMS.txt` write them. */
object Sha256 {
    const val ALGORITHM = "SHA-256"

    /** [bytes] of a finished digest as hex. */
    fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    /** The SHA-256 of the file at [path]. */
    fun of(path: Path): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        DigestInputStream(Files.newInputStream(path), digest).use { it.transferTo(OutputStream.nullOutputStream()) }
        return hex(digest.digest())
    }
}
