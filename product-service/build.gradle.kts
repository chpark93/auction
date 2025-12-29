plugins {
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = System.getenv("DOCKER_IMAGE_PREFIX")?.let { "$it-service-product" }
            ?: "auction-service-product"
    }
    container {
        jvmFlags = listOf(
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0"
        )
        ports = listOf("8087")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.10")
    
    // AWS S3 SDK v2 for MinIO compatibility
    implementation(platform("software.amazon.awssdk:bom:2.20.26"))
    implementation("software.amazon.awssdk:s3")
    
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

