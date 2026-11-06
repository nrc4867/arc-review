package dev.chieppa.arc.git.parse

import java.security.MessageDigest

/** SHA-256 of some bytes, in lowercase hex. */
internal fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
