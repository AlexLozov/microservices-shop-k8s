FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -B -ntp -pl auth-service -am dependency:go-offline
COPY common/src common/src
COPY auth-service/src auth-service/src
RUN mvn -B -ntp -pl auth-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app && mkdir /data && chown app:app /data
WORKDIR /app
COPY --from=build --chown=app:app /workspace/auth-service/target/auth-service-1.0.0.jar /app/app.jar
USER 10001:10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
