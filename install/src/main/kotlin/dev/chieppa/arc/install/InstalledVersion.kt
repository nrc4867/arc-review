package dev.chieppa.arc.install

import java.nio.file.Path

/** One installed version of Arc: its [version], its copied [jar], and the [javaHome] of the runtime it runs on. */
data class InstalledVersion(val version: String, val jar: Path, val javaHome: Path)
