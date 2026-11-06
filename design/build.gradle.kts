import dev.chieppa.arc.build.GenerateIcons

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            api("dev.chieppa.stretch:stretch-theme")
            api(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "dev.chieppa.arc.design.generated"
}

fun GenerateIcons.configure(verify: Boolean) {
    icons.set(layout.projectDirectory.dir("icons"))
    target.set(layout.projectDirectory.file("src/commonMain/kotlin/dev/chieppa/arc/design/ArcIcons.kt"))
    this.verify.set(verify)
}

tasks.register<GenerateIcons>("generateIcons") { group = "arc"; configure(verify = false) }
val verifyIcons = tasks.register<GenerateIcons>("verifyIcons") { group = "verification"; configure(verify = true) }

tasks.named("check") { dependsOn(verifyIcons) }
