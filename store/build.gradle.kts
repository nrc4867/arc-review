plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    api(project(":core"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
