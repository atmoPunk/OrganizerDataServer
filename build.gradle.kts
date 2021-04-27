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
}

application {
    mainClass.set("ru.mse.dataserver.Main")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}