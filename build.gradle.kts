import java.util.*

plugins {
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("com.github.gmazzo.buildconfig") version "5.6.7" apply false
}
allprojects {
    group = "top.mrxiaom.sweet.checkout"
    version = "1.0.10"
}
subprojects {
    if (File(projectDir, "src").exists()) {
        apply(plugin = "java")
        apply(plugin = "maven-publish")
        val targetJavaVersion = 8

        repositories {
            if (Locale.getDefault().country == "CN") {
                maven("https://mirrors.huaweicloud.com/repository/maven/")
            }
            mavenCentral()
            maven("https://repo.codemc.io/repository/maven-public/")
            maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
            maven("https://repo.helpch.at/releases/")
            maven("https://jitpack.io")
            maven("https://repo.papermc.io/repository/maven-public/")
            maven("https://repo.rosewooddev.io/repository/public/")
        }

        project.extensions.configure<JavaPluginExtension> {
            disableAutoTargetJvm()
            withSourcesJar()
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

        tasks.withType<Javadoc>().configureEach {
            (options as? StandardJavadocDocletOptions)?.apply {
                locale("zh_CN")
                charset("UTF-8")
                encoding("UTF-8")
                docEncoding("UTF-8")
                addBooleanOption("keywords", true)
                addBooleanOption("Xdoclint:none", true)

                val currentJavaVersion = JavaVersion.current()
                if (currentJavaVersion > JavaVersion.VERSION_1_9) {
                    addBooleanOption("html5", true)
                }
            }
        }

        project.extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }
        }
    }
}
