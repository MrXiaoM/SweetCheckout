allprojects {
    extra["pluginBase"] = "1.7.16"
    dependencies {
        if (configurations.findByName("implementation") != null) {
            add("implementation", "de.tr7zw:item-nbt-api:2.15.6")
        }
    }
}
