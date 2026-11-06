package dev.chieppa.arc.ui.util

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/** Turns a file's bytes into text to show, or tells it is binary. */
object TextDecoding {
    /** How far into a file a NUL byte is looked for, as git does, to call it binary. */
    private const val BINARY_PROBE = 8000
    private const val BOM = '﻿'

    fun isBinary(bytes: ByteArray): Boolean = (0 until minOf(bytes.size, BINARY_PROBE)).any { bytes[it] == 0.toByte() }

    /** [bytes] as UTF-8 text without a leading byte-order mark, or Latin-1 when they are not UTF-8; null when binary. */
    fun decode(bytes: ByteArray): String? {
        if (isBinary(bytes)) return null
        val text = try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            String(bytes, StandardCharsets.ISO_8859_1)
        }
        return text.removePrefix(BOM.toString())
    }
}
