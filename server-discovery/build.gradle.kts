import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("com.google.cloud.tools.jib")
}

group = "com.ch.auction"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = System.getenv("DOCKER_IMAGE_PREFIX")?.let { "$it-server-discovery" }
            ?: "auction-server-discovery"
    }
    container {
        jvmFlags = listOf("-Xms256m", "-Xmx512m", "-XX:+UseContainerSupport")
        ports = listOf("8761")
        environment = mapOf("SPRING_PROFILES_ACTIVE" to "prod")
    }
}
