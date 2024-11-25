FROM eclipse-temurin:21-alpine
LABEL authors="teixeira-fernando"
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]