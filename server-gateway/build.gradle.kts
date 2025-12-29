plugins {
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = System.getenv("DOCKER_IMAGE_PREFIX")?.let { "$it-server-gateway" }
            ?: "auction-server-gateway"
    }
    container {
        jvmFlags = listOf(
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseContainerSupport"
        )
        ports = listOf("8081")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.10")

    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
}
