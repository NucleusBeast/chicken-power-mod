plugins {
    id("net.neoforged.moddev") version "2.0.148" apply false
}

stonecutter active "26.3"


// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${properties["mod.version"]}\";"
    swaps["minecraft"] = "\"${current.version}\";"
    constants["release"] = properties["mod.id"] != "template"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}

// Builds every version and copies the jars to build/libs/<minecraft version>/<mod id>-<mod version>.jar.
tasks.register("buildAndCollectAll") {
    group = "build"
    description = "Builds all Minecraft versions and collects the jars in one folder"
    dependsOn(stonecutter.versions.map { ":${it.project}:buildAndCollect" })
}
