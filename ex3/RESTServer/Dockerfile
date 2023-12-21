FROM ubuntu:22.04
EXPOSE 8080

#ENV LOAD_BALANCER_URL="http://localhost:8888/api"

RUN apt-get update && apt-get install -y openjdk-17-jdk

COPY build/libs/RESTServer-1.0-SNAPSHOT.jar /opt/REST-ComputationServer.jar

RUN mkdir -p /var/log/computationserver/

CMD ["/bin/java", "-jar", "/opt/REST-ComputationServer.jar"]