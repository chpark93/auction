dependencies {
    implementation(project(":module-common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.10")
    
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.795")
    
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

