FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy all modules for a full build; relies on .dockerignore to keep context small.
COPY . .
RUN mvn -pl lighting-config-server -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    LIGHTING_DB_URL="jdbc:postgresql://lighting-config-db:5432/lighting_config" \
    LIGHTING_DB_USER="lighting" \
    LIGHTING_DB_PASSWORD="lighting" \
    LIGHTING_DB_DRIVER="org.postgresql.Driver" \
    LIGHTING_STORAGE_TYPE="jdbc" \
    LIGHTING_CONSOLE_TOKENS="lighting-console-token"

COPY --from=builder /workspace/lighting-config-server/target/lighting-config-server-*.jar /app/lighting-config-server.jar

EXPOSE 7086
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/lighting-config-server.jar"]
