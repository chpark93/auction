// 공통 Jib 설정 파일
// 각 서비스 모듈에서 apply from: "$rootDir/jib-config.gradle.kts" 로 사용

import com.google.cloud.tools.jib.gradle.JibExtension

configure<JibExtension> {
    from {
        image = "eclipse-temurin:21-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    
    to {
        // GitHub Actions에서 -Djib.to.image로 오버라이드됨
        image = project.findProperty("jib.to.image")?.toString() 
            ?: "${System.getenv("DOCKER_USERNAME") ?: "your-dockerhub-username"}/auction-${project.name}:latest"
        
        auth {
            username = System.getenv("DOCKER_USERNAME") ?: project.findProperty("dockerUsername")?.toString()
            password = System.getenv("DOCKER_PASSWORD") ?: project.findProperty("dockerPassword")?.toString()
        }
    }
    
    container {
        jvmFlags = listOf(
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0",
            "-Djava.security.egd=file:/dev/./urandom",
            "-Dspring.profiles.active=prod"
        )
        
        ports = listOf("8080")
        
        labels.set(mapOf(
            "maintainer" to "auction-team",
            "version" to project.version.toString(),
            "description" to "Auction Microservice - ${project.name}"
        ))
        
        creationTime.set("USE_CURRENT_TIMESTAMP")
        
        // 환경 변수 (필요시 추가)
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod",
            "TZ" to "Asia/Seoul"
        )
    }
    
    // Docker Daemon이 없어도 이미지 빌드 가능
    // GitHub Actions에서 Docker Hub에 직접 Push
    allowInsecureRegistries = false
}

