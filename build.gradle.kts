plugins {
    java
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.xerial:sqlite-jdbc:3.34.0")
    implementation ("com.google.code.gson:gson:2.8.6")
    implementation("commons-io:commons-io:2.6")
    implementation("com.google.api-client:google-api-client:1.23.0")
    implementation( "com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev83-1.23.0")
    implementation("javax.mail:mail:1.5.0-b01")
}

application {
    mainClass.set("ru.mse.dataserver.Main")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}