plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "com.github.heroslender.herochat"
version = "1.0-SNAPSHOT"

val javaVersion = 25

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))

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

tasks {
    register<Exec>("stopContainer") {
        commandLine("docker", "stop", "hytale-server")

        doLast { println("Container stopped") }
    }

    register<Copy>("deployToServer") {
        dependsOn("shadowJar")
        from(shadowJar.get().archiveFile)
        into(File("C:\\Users\\bruno\\Desktop\\hytale-server\\data\\mods"))

        doLast { println("Plugin deployed") }
    }

    register<Exec>("startContainer") {
        commandLine("docker", "start", "hytale-server")

        doLast { println("Container started") }
    }
}