plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "edu.trincoll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktor_version: String by project

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}