plugins {
    java
    jacoco
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.rizzleworks"

val gitDescribe = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*")
    isIgnoreExitValue = true
}

// handle scenario where .git not available (e.g. source download, tagless clone)
// runCatching also handles missing git binaries
version = runCatching {
    if (gitDescribe.result.get().exitValue == 0) {
        gitDescribe.standardOutput.asText.get().trim().removePrefix("v")
    } else {
        null
    }
}.getOrNull() ?: "0.0.0-dev"

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
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    testImplementation("io.papermc.paper:paper-api:26.2.build.121-stable")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}

tasks.processResources {
    // Read at configuration time. Touching project inside filesMatching happens at
    // execution time, which Gradle 10 rejects and the configuration cache forbids.
    val pluginVersion = project.version.toString()

    // Without this the task stays up to date across a version change, baking a
    // stale version into paper-plugin.yml.
    inputs.property("version", pluginVersion)

    filesMatching("paper-plugin.yml") {
        expand("version" to pluginVersion)
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
