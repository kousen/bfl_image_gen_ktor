plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "edu.trincoll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.0"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    testImplementation(kotlin("test"))

    implementation("ch.qos.logback:logback-classic:1.5.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}