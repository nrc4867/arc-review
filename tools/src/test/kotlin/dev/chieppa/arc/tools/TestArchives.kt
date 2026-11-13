package dev.chieppa.arc.tools

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Small archives written by tests, file name to text. */
object TestArchives {
    fun zip(target: Path, files: Map<String, String>) {
        ZipOutputStream(Files.newOutputStream(target)).use { zip ->
            files.forEach { (name, text) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray())
                zip.closeEntry()
            }
        }
    }

    /** A gzipped ustar archive; names ending in `/` are folders, [executable] files get mode 755, and [links] are symbolic links, name to target. */
    fun tarGz(target: Path, files: Map<String, String>, executable: Set<String> = emptySet(), links: Map<String, String> = emptyMap()) {
        val out = ByteArrayOutputStream()
        files.forEach { (name, text) ->
            val bytes = text.toByteArray()
            val folder = name.endsWith("/")
            out.write(header(name, if (folder) 0 else bytes.size, if (name in executable || folder) "0000755" else "0000644", if (folder) '5' else '0'))
            if (!folder) {
                out.write(bytes)
                out.write(ByteArray((512 - bytes.size % 512) % 512))
            }
        }
        links.forEach { (name, target) -> out.write(header(name, 0, "0000777", '2', target)) }
        out.write(ByteArray(1024))
        GZIPOutputStream(Files.newOutputStream(target)).use { it.write(out.toByteArray()) }
    }

    private fun header(name: String, size: Int, mode: String, type: Char, link: String = ""): ByteArray {
        val block = ByteArray(512)
        fun put(at: Int, text: String) = text.toByteArray().copyInto(block, at)
        put(0, name)
        put(100, mode)
        put(108, "0000000")
        put(116, "0000000")
        put(124, size.toString(8).padStart(11, '0'))
        put(136, "00000000000")
        block[156] = type.code.toByte()
        put(157, link)
        put(257, "ustar")
        put(263, "00")
        "        ".toByteArray().copyInto(block, 148)
        val sum = block.sumOf { it.toInt() and 0xff }
        put(148, sum.toString(8).padStart(6, '0') + "\u0000 ")
        return block
    }
}
