plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    api(project(":core"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
