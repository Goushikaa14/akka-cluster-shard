FROM openjdk:11

WORKDIR /app

COPY ./target/*.jar ./test-service.jar

CMD ["java", "-jar", "test-service.jar"]
