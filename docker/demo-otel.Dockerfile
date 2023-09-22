
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
RUN ls
#COPY ../pom.xml /app
#COPY ./* /app
#RUN ls -la
#RUN mvn clean compile -pl :demo-otel spring-boot:run -Dmaven.test.skip=true

