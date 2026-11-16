dependencyResolutionManagement {
    // The build's own version catalog, so build logic pins its tools beside the libraries the app uses.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
