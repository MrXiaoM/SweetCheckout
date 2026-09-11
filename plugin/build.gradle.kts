buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.8.1")
}
allprojects {
    val pluginBase = top.mrxiaom.gradle.LibraryHelper(project)
    extra["base"] = pluginBase
    extra["adventureVersion"] = "4.25.0"
    dependencies {
        if (configurations.findByName("implementation") != null) {
            add("implementation", "de.tr7zw:item-nbt-api:2.16.0")
        }
    }
}
