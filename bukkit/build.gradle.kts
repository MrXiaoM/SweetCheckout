plugins {
    id ("com.github.johnrengelman.shadow")
}

val shadowGroup = "top.mrxiaom.sweet.checkout.libs"

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.2.7")


    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("de.tr7zw:item-nbt-api:2.12.3")
    implementation("com.zaxxer:HikariCP:4.0.3") { isTransitive = false }
    implementation("org.jetbrains:annotations:21.0.0")
    implementation("com.github.MrXiaoM:PluginBase:1+")
}
tasks {
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
	        "top.mrxiaom.pluginbase" to "base",
            "com.zaxxer.hikari" to "hikari",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "net.kyori" to "kyori",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
