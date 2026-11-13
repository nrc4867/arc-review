package dev.chieppa.arc.install

import java.io.File
import java.nio.file.Path

/**
 * Where this process runs from: the [jar] it was started from with that jar's [version], none when it runs from a
 * class path of folders and module jars as `:app:run` does, and the [javaHome] of the runtime running it. [launched]
 * is set when Arc's own launcher started it.
 */
data class SelfLocation(val jar: Path?, val version: String?, val javaHome: Path, val launched: Boolean) {
    companion object {
        /** This process. */
        fun current(): SelfLocation {
            val jar = singleJar(System.getProperty(CLASS_PATH).orEmpty())
            return SelfLocation(
                jar,
                jar?.let { runCatching { ArcJar(it).version() }.getOrNull() },
                Path.of(System.getProperty(JAVA_HOME)).toAbsolutePath().normalize(),
                System.getProperty(InstallConstants.LAUNCHED_PROPERTY) != null,
            )
        }

        /** The jar a class path of exactly one jar names, or null. */
        fun singleJar(classPath: String): Path? = classPath
            .takeIf { File.pathSeparator !in it && it.endsWith(InstallConstants.JAR_SUFFIX, ignoreCase = true) }
            ?.let { Path.of(it).toAbsolutePath().normalize() }

        private const val CLASS_PATH = "java.class.path"
        private const val JAVA_HOME = "java.home"
    }
}
