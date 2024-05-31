plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

group = "io.github.aeckar"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.aeckar"
            artifactId = "kanary"
            version = "1.0-SNAPSHOT"

            from(components["kotlin"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.dokkaHtml {
    delete("docs/")

    pluginsMapConfiguration.set(
        mapOf("org.jetbrains.dokka.base.DokkaBase" to "{ \"footerMessage\": \"© 2024 Angel Eckardt\" }")
    )

    dokkaSourceSets {
        configureEach {
            outputDirectory.set(file("docs/"))
            reportUndocumented.set(true)
            includes.from(project.files(), "packages.md")
        }
    }
}

kotlin {
    jvmToolchain(17)
}