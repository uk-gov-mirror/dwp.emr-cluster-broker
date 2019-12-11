
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.21"
    kotlin("plugin.spring") version "1.3.21"
}

group = "uk.gov.dwp.dataworks"

repositories {
    mavenCentral()
    jcenter()
}

tasks.bootJar {
    launchScript()
}

configurations.all {
    exclude(group="org.slf4j", module="slf4j-log4j12")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons:commons-lang3:3.9")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.3")
    testImplementation("io.kotlintest:kotlintest-extensions-spring:3.3.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}