FROM alpine/git
WORKDIR /home/Bean
RUN git clone https://github.com/xirado/beanbot.git

FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY --from=0 /app/spring-petclinic /app
RUN mvn install

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=1 /app/target/spring-petclinic-1.5.1.jar /app
CMD ["java -jar spring-petclinic-1.5.1.jar"]