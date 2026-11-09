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
    api(project(":git"))
    api(project(":host"))
    api(project(":index"))
    implementation(project(":store"))
    api("dev.chieppa.stretch:stretch-core")
    api("dev.chieppa.stretch:stretch-ui")
    api("dev.chieppa.stretch:stretch-frame")
    api("dev.chieppa.stretch:stretch-diff")
    api("dev.chieppa.stretch:stretch-code")
    api("dev.chieppa.stretch:stretch-markdown")
    api("dev.chieppa.stretch:stretch-console")
    implementation("dev.chieppa.stretch:stretch-document")

    testImplementation(kotlin("test"))
    testImplementation("dev.chieppa.stretch:stretch-test")
    testImplementation(compose.desktop.currentOs)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.coroutines.swing)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
