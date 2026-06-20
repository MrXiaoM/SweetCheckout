val base: top.mrxiaom.gradle.LibraryHelper by project.extra
dependencies {
    compileOnly(base.modules.actions)
    compileOnly(base.depend.annotations)
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    for (proj in subprojects) {
        implementation(proj)
        proj.dependencies.compileOnly(base.depend.annotations)
        if (proj.name != "shared") {
            proj.dependencies.implementation(project(":plugin:nms:shared"))
        }
    }
}
