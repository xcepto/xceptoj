FROM gradle:8.5.0-jdk21 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle :examples.warehouse.shipping:shadowJar -x test

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/examples.warehouse.shipping/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
