FROM ubuntu:22.04
EXPOSE 8888

RUN apt-get update && apt-get install -y openjdk-17-jdk

COPY build/libs/LoadBalancer-1.0-SNAPSHOT.jar /opt/REST-LoadBalancer.jar

RUN mkdir -p /var/log/loadbalancer/

CMD ["/bin/java", "-jar", "/opt/REST-LoadBalancer.jar"]