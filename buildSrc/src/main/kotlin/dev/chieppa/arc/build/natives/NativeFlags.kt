package dev.chieppa.arc.build.natives

/** The `zig cc` flags every native library is compiled with, besides its target, includes, sources and output. */
object NativeFlags {
    val COMMON: List<String> = listOf("-shared", "-O2", "-fPIC", "-std=c11", "-s")
}
