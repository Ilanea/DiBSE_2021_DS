FROM ubuntu:22.04
EXPOSE 8888

RUN apt-get update && apt-get install -y openjdk-17-jdk

COPY build/libs/ChordNode-1.0-SNAPSHOT.jar /opt/ChordNode.jar

RUN mkdir -p /var/log/chordnode/

CMD ["/bin/java", "-jar", "/opt/ChordNode.jar"]