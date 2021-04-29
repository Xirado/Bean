FROM adoptopenjdk:15-jdk-hotspot
WORKDIR /home/bean/
RUN apt update -y
RUN apt install git -y
RUN apt install maven -y
RUN git clone https://github.com/Xirado/beanbot.git .
RUN mvn clean compile assembly:single
COPY target/*.jar Bean.jar
ENTRYPOINT java -server -Xmx10G -Dnogui=true -jar Bean.jar