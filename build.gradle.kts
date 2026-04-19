plugins {
    java
    jacoco
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "com.rizzleworks"
version = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--always")
}.standardOutput.asText.map { it.trim().removePrefix("v") }.getOrElse("0.0.0-dev")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.register<Copy>("deploy") {
    description = "Build and deploy the plugin jar to a local Paper server"
    group = "distribution"

    val dir = project.findProperty("deployDir") as String?
        ?: throw GradleException("Set deployDir in gradle.properties (e.g. deployDir=../my-server/plugins)")

    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(dir)

    doLast {
        logger.lifecycle("Deployed to: $dir")
    }
}
