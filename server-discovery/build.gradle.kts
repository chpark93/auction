plugins {
    id("com.google.cloud.tools.jib")
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
        jvmFlags = listOf(
            "-Xms256m",
            "-Xmx512m",
            "-XX:+UseContainerSupport"
        )
        ports = listOf("8761")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
}

