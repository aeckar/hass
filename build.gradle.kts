plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "com.github.aeckar"
version = "2.1"

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("reflect"))
    implementation("com.github.aeckar:once:1.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.aeckar"
            artifactId = "kanary"
            version = "2.1"

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