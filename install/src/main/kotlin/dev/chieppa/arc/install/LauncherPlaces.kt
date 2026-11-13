package dev.chieppa.arc.install

import java.nio.file.Path

/**
 * Where launchers go: the Start menu's programs folder and the taskbar's pinned shortcuts on Windows, the person's
 * Applications folder on macOS.
 */
data class LauncherPlaces(val startMenu: Path, val taskbarPins: Path, val applications: Path) {
    companion object {
        private const val APPDATA = "APPDATA"
        private const val HOME = "user.home"
        private const val ROAMING = "AppData/Roaming"
        private const val START_MENU = "Microsoft/Windows/Start Menu/Programs"
        private const val TASKBAR = "Microsoft/Internet Explorer/Quick Launch/User Pinned/TaskBar"
        private const val APPLICATIONS = "Applications"

        /** This person's places, from `APPDATA` and the home folder. */
        fun system(env: Map<String, String> = System.getenv(), home: Path = Path.of(System.getProperty(HOME))): LauncherPlaces {
            val roaming = env[APPDATA]?.let(Path::of) ?: home.resolve(ROAMING)
            return LauncherPlaces(roaming.resolve(START_MENU), roaming.resolve(TASKBAR), home.resolve(APPLICATIONS))
        }
    }
}
