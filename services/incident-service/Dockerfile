FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/incident-service-0.0.1-SNAPSHOT.jar /app/incident-manager.jar

EXPOSE 6567

CMD ["java", "-jar", "incident-manager.jar"]