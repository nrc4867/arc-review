package dev.chieppa.arc.syntax.wasm

import java.security.MessageDigest

/** A query's key in a [Snapshot]: the SHA-256 of its source in UTF-8, in hex. */
internal object QueryHash {
    fun of(source: String): String =
        MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
