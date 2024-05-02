plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "com.github.eckar"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.ladle-gh"
            artifactId = "kanary"
            version = "1.1"

            from(components["kotlin"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}