import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id ("com.github.johnrengelman.shadow")
}

val targetJavaVersion = 17
val shadowGroup = "top.mrxiaom.sweet.checkout.backend.libs"
val entry = "top.mrxiaom.sweet.checkout.backend.ConsoleMain"

@Suppress("VulnerableLibrariesLocal")
dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.jline:jline-terminal-jansi:3.21.0")
    implementation("net.minecrell:terminalconsoleappender:1.3.0")
    implementation("org.slf4j:slf4j-api:2.0.16")

    // alipay sdk and wechat pay sdk
    implementation("org.bouncycastle:bcprov-jdk15on:1.62")
    implementation("dom4j:dom4j:1.6.1")
    implementation("com.squareup.okhttp3:okhttp:3.12.13")

    implementation("commons-io:commons-io:2.17.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("top.mrxiaom:Java-WebSocket:1.5.8")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.jetbrains:annotations:21.0.0")
    implementation(project(":packets"))
}

java {
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
        archiveClassifier.set("")
        destinationDirectory.set(rootProject.file("out"))
        minimize {
            val dependencies = listOf(
                "org.bouncycastle:bcprov-jdk15on",
                "commons-io:commons-io",
            )
            include {
                dependencies.contains("${it.moduleGroup}:${it.moduleName}")
            }
            mergeServiceFiles()
            transform(Log4j2PluginsCacheFileTransformer())
        }
        setupManifest()
    }
    build {
        dependsOn(shadowJar)
    }
    create<JavaExec>("runConsole") {
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
