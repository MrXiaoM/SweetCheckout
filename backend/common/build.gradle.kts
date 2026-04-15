val targetJavaVersion = 8

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")

    val dependencies: List<String> by project.extra
    for (dependency in dependencies) {
        compileOnly(dependency)
    }

    compileOnly(project(":packets"))
}

java {
    withJavadocJar()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}
