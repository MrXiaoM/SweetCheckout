val sharedDependencies = listOf(
    "org.slf4j:slf4j-api:2.0.16",

    // alipay sdk and wechat pay sdk
    "org.bouncycastle:bcprov-jdk15on:1.62",
    "dom4j:dom4j:1.6.1",
    "com.squareup.okhttp3:okhttp:3.12.13",
    // paypal sdk
    "io.github.eealba:jasoner:1.0.0",

    "commons-io:commons-io:2.17.0",
    "com.google.code.gson:gson:2.10",
    "top.mrxiaom:Java-WebSocket:1.5.8",
)
project(":plugin:bukkit:with-backend").extra["backendDependencies"] = sharedDependencies
subprojects {
    extra["dependencies"] = sharedDependencies
}
