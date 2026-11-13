plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("dev.chieppa.stretch.app")
}

// Arc's name, id and icon: drawn from design/app-icon (arc-16.svg, arc-24.svg and the like beside arc.svg are drawn
// for their sizes), put on the windows, and carried in the jar for the launchers Arc makes when it installs itself.
stretchApp {
    name = "Arc"
    id = "dev.chieppa.arc"
    icon = rootProject.layout.projectDirectory.file("design/app-icon/arc.svg")
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":store"))
    implementation(project(":install"))
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

// The uber jar Arc ships as, one per OS: it may call restricted methods (skiko loading its library, FFM for
// tree-sitter, the keychain and the Start menu shortcut) without Java warning on every run, and carries its version,
// which installing and updating compare.
tasks.withType<org.gradle.jvm.tasks.Jar>().matching { it.name.startsWith("packageUberJar") }.configureEach {
    manifest.attributes("Enable-Native-Access" to "ALL-UNNAMED", "Implementation-Version" to project.version.toString())
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
