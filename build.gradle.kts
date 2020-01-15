import com.google.cloud.tools.jib.gradle.JibExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.2.RELEASE"
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
    implementation("org.springdoc:springdoc-openapi-core:1.1.49")
    // AWS things
    implementation(platform("software.amazon.awssdk:bom:2.10.24"))
    implementation("software.amazon.awssdk:emr")
    implementation("software.amazon.awssdk:ec2")
    // Monitoring things
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-core:1.3.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.3.2")
    // Logging things
    implementation("org.slf4j:slf4j-api:1.7.30")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("ch.qos.logback:logback-core:1.2.3")
    // General things
    implementation("org.apache.commons:commons-lang3:3.9")
    // Test things
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("io.mockk:mockk:1.8.13.kotlin13")
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

configure<JibExtension> {
    to {
        image = "dwpdigital/emr-cluster-broker"
    }
}
