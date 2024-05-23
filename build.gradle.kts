plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "com.github.aeckar"
version = "3.2"

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.aeckar"
            artifactId = "kanary"
            version = "3.2"

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