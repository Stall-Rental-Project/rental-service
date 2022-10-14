FROM openjdk:11

# Set the working directory
WORKDIR /app

COPY /target/rental-service.jar rental-service.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar", "rental-service.jar"]

EXPOSE 9567