buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.32")
}
allprojects {
    val pluginBase = top.mrxiaom.gradle.LibraryHelper(project)
    extra["base"] = pluginBase
    dependencies {
        if (configurations.findByName("implementation") != null) {
            add("implementation", "de.tr7zw:item-nbt-api:2.16.0")
        }
    }
}
