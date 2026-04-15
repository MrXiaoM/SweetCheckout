val base: top.mrxiaom.gradle.LibraryHelper by project.extra
dependencies {
    compileOnly(base.modules.actions)
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.0")
    for (proj in subprojects) {
        implementation(proj)
        proj.dependencies.compileOnly("org.jetbrains:annotations:24.0.0")
        if (proj.name != "shared") {
            proj.dependencies.implementation(project(":plugin:nms:shared"))
        }
    }
}
