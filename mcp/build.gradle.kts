plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

dependencies {
    api(project(":plugins"))
    // The official MCP Kotlin SDK (MIT and Apache-2.0) for the protocol and its streamable HTTP transport, served by
    // Ktor's CIO engine (Apache-2.0).
    implementation(libs.mcp.sdk.server)
    implementation(libs.ktor.server.cio)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
