plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "com.github.heroslender.herochat"
version = "v1.3.1"

val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/hytale/")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:2026.01.28-87d03be09")

    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("at.helpch:placeholderapi-hytale:1.0.4")

    testImplementation(kotlin("test"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        mergeServiceFiles()

        manifest {
            attributes(
                "Multi-Release" to "true"
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

    processResources {
        filesMatching("manifest.json") {
            expand(
                mapOf(
                    "version" to version,
                )
            )
        }
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
