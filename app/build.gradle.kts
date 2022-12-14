plugins {
    kotlin("jvm") version "1.7.22"
    id("io.ktor.plugin") version "2.1.3"
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

val aapLibsVersion = "3.5.28"
val ktorVersion = "2.1.3"

dependencies {
    implementation("com.github.navikt.aap-libs:kafka:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-utils:$aapLibsVersion")
    implementation("com.github.navikt.aap-libs:ktor-auth-azuread:$aapLibsVersion")
    implementation("com.github.navikt:aap-vedtak:1.0.247")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:1.10.0")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.0")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:$aapLibsVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "18"
    }

    withType<Test> {
        reports.html.required.set(false)
        useJUnitPlatform()
    }
}

application {
    mainClass.set("aap.bot.AppKt")
}
