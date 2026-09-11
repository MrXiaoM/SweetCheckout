subprojects {
    val base: top.mrxiaom.gradle.LibraryHelper by project.extra
    val adventureVersion: String by project.extra
    extra["dependencies"] = listOf(
        base.modules.library,
        base.modules.message,
        base.modules.actions,
        base.modules.l10n,
        base.modules.temporaryData,
        base.modules.paper,
        base.modules.misc,
        base.resolver.lite,
    )
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
    )
    dependencies {
        add("compileOnly", "org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
        add("compileOnly", "me.clip:placeholderapi:2.12.2")
    }
}
