subprojects {
    val pluginBase = "1.7.13"
    fun String.module(module: String): String {
        return "top.mrxiaom.pluginbase:$module:$this"
    }
    extra["pluginBase"] = pluginBase
    extra["dependencies"] = mapOf(
        "com.github.technicallycoded:FoliaLib:0.4.4" to true,
        pluginBase.module("library") to false,
        pluginBase.module("actions") to false,
        pluginBase.module("l10n") to false,
        pluginBase.module("temporary-data") to false,
        pluginBase.module("paper") to false,
        "top.mrxiaom:LibrariesResolver-Lite:$pluginBase" to false,
    )
    extra["libraries"] = listOf(
        "top.mrxiaom:qrcode-encoder:1.0.0",
        "net.kyori:adventure-api:4.22.0",
        "net.kyori:adventure-platform-bukkit:4.4.0",
        "net.kyori:adventure-text-minimessage:4.22.0",
        "com.zaxxer:HikariCP:4.0.3",
        "top.mrxiaom:EvalEx-j8:3.4.0",
    )
    extra["shadowRelocations"] = mapOf(
        "top.mrxiaom.pluginbase" to "base",
        "de.tr7zw.changeme.nbtapi" to "nbtapi",
        "org.java_websocket" to "websocket",
        "com.tcoded.folialib" to "folialib",
    )
    dependencies {
        add("compileOnly", "org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
        add("compileOnly", "me.clip:placeholderapi:2.11.6")
    }
}
