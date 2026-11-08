package dev.chieppa.arc.build.natives

/** One zig release archive for one host: where it is and the SHA-256 it must have. */
data class ZigDownload(val url: String, val sha256: String) {
    /** The archive's file name, such as `zig-x86_64-windows-0.17.1.zip`. */
    val fileName: String get() = url.substringAfterLast('/')

    /** The folder the archive unpacks to, such as `zig-x86_64-windows-0.17.1`. */
    val folderName: String get() = fileName.removeSuffix(ZIP).removeSuffix(TAR_XZ)

    /** Whether the archive is a zip; otherwise it is a `.tar.xz`. */
    val isZip: Boolean get() = fileName.endsWith(ZIP)

    private companion object {
        const val ZIP = ".zip"
        const val TAR_XZ = ".tar.xz"
    }
}
