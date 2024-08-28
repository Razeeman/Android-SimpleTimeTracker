rootProject.name = "Simple Time Tracker"
include(
    "app",
    "domain",
    "core",
    "navigation",
    "data_local",
    "resources",
    "wear",
    "wear_api",
)

file("features").walkTopDown().maxDepth(2).forEach { dir ->
    val isFeatureModule = dir.name.startsWith("feature_")
    val isSubModule = dir.name in listOf("api", "views")

    if (dir.isDirectory && isFeatureModule) {
        include(dir.name)
        project(":${dir.name}").projectDir = dir
    }
    if (dir.isDirectory && isSubModule) {
        include(":${dir.parentFile.name}:${dir.name}")
        project(":${dir.parentFile.name}:${dir.name}").projectDir = dir
    }
}
