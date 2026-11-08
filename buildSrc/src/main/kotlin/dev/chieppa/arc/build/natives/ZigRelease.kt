package dev.chieppa.arc.build.natives

/** The zig release that builds the native libraries, pinned by version and by each host archive's SHA-256. */
object ZigRelease {
    const val VERSION = "0.17.1"
    private const val BASE = "https://ziglang.org/download/$VERSION"

    /** The archive for each host, keyed as zig names hosts (`x86_64-windows`), from ziglang.org/download/index.json. */
    val downloads: Map<String, ZigDownload> = mapOf(
        "x86_64-windows" to ZigDownload(
            "$BASE/zig-x86_64-windows-$VERSION.zip",
            "b03f477a1943673e02aca0f570e8030dffd536ce1f45cbc4d3326ec31159562e",
        ),
        "aarch64-macos" to ZigDownload(
            "$BASE/zig-aarch64-macos-$VERSION.tar.xz",
            "75f1bc7a1e9852f17ec9a0c1895f767c4ec56d46bc8a834920eece1d0359dd8d",
        ),
        "x86_64-macos" to ZigDownload(
            "$BASE/zig-x86_64-macos-$VERSION.tar.xz",
            "9af07b20319fbd0c92ee46ad23e81d6a782b1aaea2cfb8dcbff35f1d2c7ddbb7",
        ),
        "x86_64-linux" to ZigDownload(
            "$BASE/zig-x86_64-linux-$VERSION.tar.xz",
            "3581863e3cbcfd0fce0efc682ce7e9e93da96668a977791d356a26d2a7e9b17a",
        ),
    )
}
