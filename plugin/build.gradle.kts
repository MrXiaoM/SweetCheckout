buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.17")
}
allprojects {
    val pluginBase = top.mrxiaom.gradle.LibraryHelper(project)
    extra["base"] = pluginBase
    dependencies {
        if (configurations.findByName("implementation") != null) {
            add("implementation", pluginBase.depend.nbtapi)
        }
    }
}
