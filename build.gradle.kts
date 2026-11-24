import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService

plugins {
    id("net.neoforged.moddev")
}

version = "${sc.properties["mod.version"]}+${sc.current.version}"
base.archivesName = sc.properties["mod.id"]

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {

}

neoForge {
    version = sc.properties["deps.neo_loader"]

    mods {
        register(sc.properties["mod.id"]) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }

        register("server") {
            gameDirectory = file("../../run/")
            server()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
            // NeoForge 26.2+ splits `logoFile` into `bannerFile` (wide) and `iconFile` (square)
            register("logo_key", if (sc.current.parsed >= "26.2") "iconFile" else "logoFile")
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        // Recipes in src/ use the 1.21.2+ ingredient format: "minecraft:stone" or "#minecraft:planks".
        // 1.21.1 needs objects: {"item": "minecraft:stone"} or {"tag": "minecraft:planks"}.
        if (sc.current.parsed < "1.21.2") doLast {
            fun legacy(value: Any?): Any? = when (value) {
                is String -> if (value.startsWith("#")) mapOf("tag" to value.substring(1)) else mapOf("item" to value)
                is List<*> -> value.map(::legacy)
                else -> value
            }
            val recipes = (this as ProcessResources).destinationDir.resolve("data")
            recipes.walk().filter { it.isFile && it.extension == "json" && it.parentFile.name == "recipe" }.forEach { file ->
                @Suppress("UNCHECKED_CAST")
                val recipe = groovy.json.JsonSlurper().parse(file) as MutableMap<String, Any?>
                (recipe["key"] as? MutableMap<String, Any?>)?.replaceAll { _, value -> legacy(value) }
                (recipe["ingredients"] as? List<*>)?.let { recipe["ingredients"] = legacy(it) }
                recipe["ingredient"]?.let { recipe["ingredient"] = legacy(it) }
                file.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(recipe)))
            }
        }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
        usesService(StonecutterTaskMutexService.create(gradle, name))
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val name = sc.properties["mod.id"]
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "$it-$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to `build/libs/{minecraft version}/{mod id}-{mod version}.jar`"

        val outName = "${sc.properties["mod.id"]}-${sc.properties["mod.version"]}.jar"
        inputs.property("out_name", outName)
        from(jar.flatMap { it.archiveFile })
        rename { outName }
        into(rootProject.layout.buildDirectory.dir("libs/${sc.current.version}"))
    }
}
