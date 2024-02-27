rootProject.name = "Simple Time Tracker"
include(
    "app",
    "domain",
    "core",
    "navigation",
    "data_local",
    "resources",
)

file("features").walkTopDown().maxDepth(1).forEach { dir ->
    if (dir.isDirectory) {
        include(dir.name)
        project(":${dir.name}").projectDir = dir
    }
}
include(":wear")
include(":wearrpc")
