plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("at.favre.lib:bcrypt:0.10.2")
}

tasks.test {
    useJUnitPlatform()
}