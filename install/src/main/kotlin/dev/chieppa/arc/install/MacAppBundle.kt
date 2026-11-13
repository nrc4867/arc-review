package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermissions

/**
 * Arc on macOS: `Arc.app` in the person's Applications folder, whose program is a script that runs the runtime's java
 * on `launch.args` with Arc's name and icon in the Dock. The bundle is written beside its place and moved in, then
 * touched so Launch Services reads it again.
 */
class MacAppBundle(private val layout: InstallLayout, private val places: LauncherPlaces) : Launcher {
    /** Where the bundle goes. */
    val bundle: Path get() = places.applications.resolve(InstallConstants.APP_NAME + APP)

    override fun write(javaHome: Path, version: String) {
        AtomicFiles.replaceFolder(bundle) { folder ->
            val contents = folder.resolve(CONTENTS)
            Files.createDirectories(contents.resolve(MAC_OS))
            Files.createDirectories(contents.resolve(RESOURCES))
            Files.writeString(contents.resolve(INFO_PLIST), infoPlist(version))
            if (Files.isRegularFile(layout.icns)) Files.copy(layout.icns, contents.resolve(RESOURCES).resolve(InstallConstants.ICNS))
            val program = contents.resolve(MAC_OS).resolve(InstallConstants.APP_NAME)
            Files.writeString(program, script(JavaPrograms.windowed(javaHome)))
            runCatching { Files.setPosixFilePermissions(program, PosixFilePermissions.fromString(EXECUTABLE)) }
        }
        Files.setLastModifiedTime(bundle, FileTime.fromMillis(System.currentTimeMillis()))
    }

    override fun exists(): Boolean = Files.isRegularFile(bundle.resolve(CONTENTS).resolve(INFO_PLIST))

    override fun remove() = AtomicFiles.deleteTree(bundle)

    override fun start() {
        ProcessBuilder(OPEN, bundle.toString()).start()
    }

    /** The bundle's `Info.plist` for [version]. */
    fun infoPlist(version: String): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
          <key>CFBundleName</key><string>${InstallConstants.APP_NAME}</string>
          <key>CFBundleDisplayName</key><string>${InstallConstants.APP_NAME}</string>
          <key>CFBundleIdentifier</key><string>${InstallConstants.APP_ID}</string>
          <key>CFBundleExecutable</key><string>${InstallConstants.APP_NAME}</string>
          <key>CFBundleIconFile</key><string>${InstallConstants.ICNS}</string>
          <key>CFBundlePackageType</key><string>APPL</string>
          <key>CFBundleShortVersionString</key><string>${xml(version)}</string>
          <key>CFBundleVersion</key><string>${xml(version)}</string>
          <key>LSMinimumSystemVersion</key><string>$MINIMUM_MACOS</string>
          <key>NSHighResolutionCapable</key><true/>
        </dict>
        </plist>
    """.trimIndent() + "\n"

    /** The bundle's program: replaces itself with [java] on `launch.args`, so Arc is the bundle's own process. */
    fun script(java: Path): String = "#!/bin/sh\nexec " + listOf(
        java.toString(),
        DOCK_NAME + InstallConstants.APP_NAME,
        DOCK_ICON + layout.icns,
        LaunchArgs.reference(layout.launchArgs),
    ).joinToString(" ") { quoted(it) } + " \"\$@\"\n"

    private fun quoted(text: String) = "'" + text.replace("'", "'\''") + "'"

    private fun xml(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private companion object {
        const val APP = ".app"
        const val CONTENTS = "Contents"
        const val MAC_OS = "MacOS"
        const val RESOURCES = "Resources"
        const val INFO_PLIST = "Info.plist"
        const val EXECUTABLE = "rwxr-xr-x"
        const val OPEN = "open"
        const val DOCK_NAME = "-Xdock:name="
        const val DOCK_ICON = "-Xdock:icon="

        /** The oldest macOS the JetBrains Runtime 25 runs on. */
        const val MINIMUM_MACOS = "12.0"
    }
}
