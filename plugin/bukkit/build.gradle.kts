subprojects {
    val base: top.mrxiaom.gradle.LibraryHelper by project.extra
    extra["dependencies"] = mapOf(
        "com.github.technicallycoded:FoliaLib:0.4.4" to true,
        base.modules.library to false,
        base.modules.actions to false,
        base.modules.l10n to false,
        base.modules.temporaryData to false,
        base.modules.paper to false,
        base.resolver.lite to false,
    )
    val adventureVersion = "4.22.0"
    extra["libraries"] = listOf(
        "top.mrxiaom:qrcode-encoder:1.0.0",
        "net.kyori:adventure-api:$adventureVersion",
        "net.kyori:adventure-text-minimessage:$adventureVersion",
        "net.kyori:adventure-text-serializer-gson:$adventureVersion",
        "net.kyori:adventure-text-serializer-plain:$adventureVersion",
        base.depend.HikariCP,
        base.depend.EvalEx,
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
