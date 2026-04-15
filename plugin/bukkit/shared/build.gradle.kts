plugins {
    id("com.github.gmazzo.buildconfig")
}
java.withJavadocJar()
val base: top.mrxiaom.gradle.LibraryHelper by project.extra
dependencies {
    val dependencies: Map<String, Boolean> by project.extra
    for ((dependency, _) in dependencies) {
        compileOnly(dependency)
    }
    val libraries: List<String> by project.extra
    for (lib in libraries) {
        base.library(lib)
    }
    compileOnly("com.github.MrXiaoM:SweetPlayerMarket:11dafcb097")
    compileOnly(project(":plugin:nms"))
    compileOnly(project(":packets"))
}

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.checkout")

    base.doResolveLibraries()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}
