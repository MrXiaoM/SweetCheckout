import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id("com.gradleup.shadow")
}

java.withJavadocJar()
val shadowGroup = "top.mrxiaom.sweet.checkout.libs"
val shadowLink = configurations.create("shadowLink")
val base: top.mrxiaom.gradle.LibraryHelper by project.extra
dependencies {
    val dependencies: Map<String, Boolean> by project.extra
    for ((dependency, ignore) in dependencies) {
        if (ignore) {
            implementation(dependency) { isTransitive = false }
        } else {
            implementation(dependency)
        }
    }
    val libraries: List<String> by project.extra
    for (library in libraries) {
        base.library(library)
    }
    val backendDependencies: List<String> by project.extra
    for (dependency in backendDependencies) {
        implementation(dependency)
    }
    add(shadowLink.name, project("java9"))
    implementation(project(":backend:common"))
    implementation(project(":plugin:bukkit:shared"))
    for (dependency in project.project(":plugin:nms").allprojects) {
        add(shadowLink.name, dependency)
    }
    implementation(project(":packets"))
}
tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        configurations.add(shadowLink)
        val shadowRelocations: Map<String, String> by project.extra
        mapOf(
            "com.google.gson" to "gson",
            "com.wechat" to "payment.wechat",
            "com.alipay" to "payment.alipay",
            "io.github.eealba.payper" to "payment.paypal",
            "okhttp3" to "okhttp3",
            "okio" to "okio",
            "javax.xml" to "xml.javax",
            "org.dom4j" to "xml.dom4j",
            "org.w3c.dom" to "xml.w3c.dom",
            "org.xml.sax" to "xml.sax",
        ).plus(shadowRelocations).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
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
    }
    val copyTask = this.register<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "SweetCheckout-bukkit-with-backend-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf(
                "version" to version,
                "libraries_config" to base.addedLibrariesYAML.joinToString("\n")
            ))
            include("plugin.yml")
        }
    }
}
