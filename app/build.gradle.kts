plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":store"))
    implementation("dev.chieppa.stretch:stretch-document")
    implementation("dev.chieppa.stretch:stretch-devtools")
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)

    testImplementation(kotlin("test"))
    testImplementation("dev.chieppa.stretch:stretch-test")
}

// -Pdemo puts the dev-only sample conversation on the classpath, so threads and staged comments show sample data.
if (providers.gradleProperty("demo").isPresent) {
    dependencies { runtimeOnly(project(":demo")) }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

compose.desktop {
    application {
        mainClass = "dev.chieppa.arc.app.MainKt"
        // The JetBrains Runtime, so StretchWindow can draw its own title bar in a native frame.
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmToolchain.get().toInt()))
            vendor.set(JvmVendorSpec.JETBRAINS)
        }.get().metadata.installationPath.asFile.absolutePath
        jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")
        // -Pdevtools=<port> serves screenshots of the windows on 127.0.0.1.
        providers.gradleProperty("devtools").orNull?.let { jvmArgs += "-Dstretch.devtools=$it" }
        // -PpluginsNode=<node> runs plugins on an installed Node, not the pinned download.
        providers.gradleProperty("pluginsNode").orNull?.let { jvmArgs += "-Darc.plugins.node=$it" }
        nativeDistributions {
            packageName = "arc-review"
            packageVersion = project.version.toString().substringBefore('-')
            modules("java.instrument", "jdk.httpserver", "jdk.unsupported")
        }
    }
}
