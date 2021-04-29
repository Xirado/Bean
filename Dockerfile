FROM openjdk:11-alpine
WORKDIR /home/bean/build
RUN git pull
RUN mvn clean compile assembly:single
WORKDIR /home/bean/
COPY build/target/beanbot-7.0.0-jar-with-dependencies.jar Bean.jar
ENTRYPOINT java -server -Xmx10G -Dnogui=true -jar Bean.jar