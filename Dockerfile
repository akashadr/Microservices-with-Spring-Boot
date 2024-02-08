FROM openjdk:17
EXPOSE 8080
ADD target/project1.jar project1.jar
ENTRYPOINT ["java","-jar","/project1.jar" ]