java.withJavadocJar()
val shadowGroup = "top.mrxiaom.sweet.checkout.libs"

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")

    val dependencies: List<String> by project.extra
    for (dependency in dependencies) {
        compileOnly(dependency)
    }
    compileOnly(project(":plugin:nms"))
    compileOnly(project(":packets"))
}
