# ================================================================
# Dockerfile — notification-service (Multi-stage, Maven)
# ================================================================

# -- Stage 1: Build ------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-jammy AS build
WORKDIR /workspace

# Copy parent POM and module POMs first (for dependency caching)
COPY pom.xml .
COPY shared-kernel/pom.xml shared-kernel/
COPY notification-service/pom.xml notification-service/
# Copy stub POMs for other modules so parent POM resolves
COPY identity-service/pom.xml identity-service/
COPY room-service/pom.xml room-service/
COPY estimation-service/pom.xml estimation-service/
COPY audit-service/pom.xml audit-service/
COPY test-fixtures/pom.xml test-fixtures/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl notification-service -am -B

# Copy source
COPY shared-kernel/src shared-kernel/src
COPY notification-service/src notification-service/src

# Build
RUN mvn package -pl notification-service -am -Dmaven.test.skip=true -B

# Extract application JAR
RUN java -Djarmode=tools -jar notification-service/target/notification-service-*.jar extract --destination extracted

# -- Stage 2: Runtime ----------------------------------------------
FROM eclipse-temurin:21-jre-jammy
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app
COPY --from=build /workspace/extracted/ ./
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080 8081
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar notification-service-0.0.1-SNAPSHOT.jar"]
