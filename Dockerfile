FROM openjdk:11

# Set the working directory
WORKDIR /app

COPY /target/rental-service.jar rental-service.jar

ENTRYPOINT ["java", "-jar", "rental-service.jar"]

EXPOSE 6567