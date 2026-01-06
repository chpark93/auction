plugins {
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = System.getenv("DOCKER_IMAGE_PREFIX")?.let { "$it-service-chat" }
            ?: "auction-service-chat"
    }
    container {
        jvmFlags = listOf(
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0"
        )
        ports = listOf("8085")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.10")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
