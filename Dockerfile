FROM openjdk:17-jdk-slim
COPY ./target/shop-0.0.1-SNAPSHOT.jar /app/shop.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "shop.jar"]