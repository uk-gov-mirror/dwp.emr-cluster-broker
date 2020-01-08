
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("com.google.cloud.tools.jib") version "1.8.0"
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
    // Kotlin things
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Spring things
    implementation("org.springframework.boot:spring-boot-starter-web")
    // AWS things
    implementation(platform("software.amazon.awssdk:bom:2.10.24"))
    implementation("software.amazon.awssdk:emr")
    // General things
    implementation("org.apache.commons:commons-lang3:3.9")

    // Test things
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.3")
    testImplementation("io.kotlintest:kotlintest-extensions-spring:3.3.3")
    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0-M1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
