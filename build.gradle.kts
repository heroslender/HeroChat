plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "com.github.heroslender.herochat"
version = "v1.6.0"

val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/hytale/")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:2026.02.19-1a311a592")

    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-nop:1.7.36")

    compileOnly("at.helpch:placeholderapi-hytale:1.0.4")

    testImplementation("com.hypixel.hytale:Server:2026.02.19-1a311a592")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
    testImplementation("net.bytebuddy:byte-buddy:1.17.8") // TEMP: Needed for java25
}

tasks {
    test {
        useJUnitPlatform()

        jvmArgs("-XX:+EnableDynamicAgentLoading")

        // Needed for mocking hytale stuff
        systemProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager")
    }

    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        mergeServiceFiles()

        relocate("org.slf4j", "com.github.heroslender.libs.slf4j")
        relocate("com.zaxxer.hikari", "com.github.heroslender.libs.hikari")

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
