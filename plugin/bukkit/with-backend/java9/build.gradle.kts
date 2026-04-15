val targetJavaVersion = 9
val base: top.mrxiaom.gradle.LibraryHelper by project.extra
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly(base.modules.library)
    compileOnly(project(":plugin:bukkit:shared"))
    compileOnly(project(":plugin:bukkit:with-backend"))
    compileOnly(project(":backend:common"))
}
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.isWarnings = false
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}
