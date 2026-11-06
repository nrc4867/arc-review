// Dev only: sample threads and staged comments for the screens that show them, until the store and the GitHub host
// provide real ones. The app puts it on its classpath only when run with -Pdemo.
plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    implementation(project(":host"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
