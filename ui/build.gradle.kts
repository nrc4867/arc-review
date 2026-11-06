plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    api(project(":core"))
    api(project(":design"))
    api("dev.chieppa.stretch:stretch-core")
    api("dev.chieppa.stretch:stretch-ui")
    api("dev.chieppa.stretch:stretch-frame")

    testImplementation(kotlin("test"))
    testImplementation("dev.chieppa.stretch:stretch-test")
    testImplementation(compose.desktop.currentOs)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
