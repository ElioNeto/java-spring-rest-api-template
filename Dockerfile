FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="api@example.com"
LABEL description="Spring Boot REST API Boilerplate"

WORKDIR /app

COPY target/boilerplate-1.0.0.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
