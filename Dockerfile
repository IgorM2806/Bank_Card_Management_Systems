
FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine

ARG JAR_FILE=target/BankCardManagementSystems-1.0-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

CMD ["java", "-jar", "app.jar"]