rootProject.name = "arc-review"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Stretch is built from source: the checkout named by stretch.dir (set it in ~/.gradle/gradle.properties),
// or a sibling folder named stretch.
val stretchDir = providers.gradleProperty("stretch.dir").orNull
    ?: file("../stretch").takeIf { it.isDirectory }?.path
    ?: error("Stretch not found: set stretch.dir in ~/.gradle/gradle.properties to your Stretch checkout")
includeBuild(stretchDir)

include(":core")
include(":syntax")
include(":codeintel")
include(":git")
include(":index")
include(":host")
include(":store")
include(":design")
include(":ui")
include(":app")

// Dev only: sample threads and staged comments, on the classpath only with -Pdemo.
include(":demo")
