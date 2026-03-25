import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id("com.gradleup.shadow")
}

val targetJavaVersion = 17
val entry = "top.mrxiaom.sweet.checkout.backend.ConsoleMain"

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.jline:jline-terminal-jansi:3.21.0")
    implementation("net.minecrell:terminalconsoleappender:1.3.0")

    val dependencies: List<String> by project.extra
    for (dependency in dependencies) {
        implementation(dependency)
    }

    implementation(project(":backend:common"))
    implementation(project(":packets"))
}

java {
    withJavadocJar()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

fun Jar.setupManifest() {
    manifest {
        attributes(
            "main-class" to entry
        )
    }
}
tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mergeServiceFiles()
        transform(Log4j2PluginsCacheFileTransformer())
        setupManifest()
    }
    val copyTask = this.register<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "backend-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    this.register<JavaExec>("runConsole") {
        group = "minecraft"
        mainClass.set(entry)
        classpath = sourceSets.main.get().runtimeClasspath
        workingDir = File(projectDir, "run").also { it.mkdirs() }
        this.standardInput = System.`in`

        defaultCharacterEncoding = "UTF-8"
        systemProperties(
            "file.encoding" to "UTF-8",
            "sun.stdout.encoding" to "UTF-8",
            "sun.stderr.encoding" to "UTF-8"
        )
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}
