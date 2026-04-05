dependencies {
    compileOnly("top.mrxiaom.pluginbase:actions:${project.extra["pluginBase"]}")
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    implementation("org.jetbrains:annotations:21.0.0")
    for (proj in subprojects) {
        implementation(proj)
        proj.dependencies.implementation("org.jetbrains:annotations:21.0.0")
        if (proj.name != "shared") {
            proj.dependencies.implementation(project(":plugin:nms:shared"))
        }
    }
}
