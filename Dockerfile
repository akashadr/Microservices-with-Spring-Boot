# FROM openjdk:17
# EXPOSE 8080
# ADD target/project1.jar project1.jar
# ENTRYPOINT ["java","-jar","/project1.jar" ]

FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .

RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /build/libs/project1-1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
