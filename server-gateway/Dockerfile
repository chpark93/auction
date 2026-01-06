FROM eclipse-temurin:21-jre-alpine

# Install curl for healthcheck
RUN apk update && apk add --no-cache curl

WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
