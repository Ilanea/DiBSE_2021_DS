This seems to be working for creating the Chord Network and after some stabilizing time creates a working network with correct fingertables
Nodes leaving the network is currently NOT working, the same for failing nodes

Build the JAR:
```
gradle build
OR
.\gradlew build
```

Build the Docker Container:
```
cd ChordNode
docker build -t chord-node .
```

Run Docker Compose:
To start all Nodes use the docker-compose.yml in the all folder, for testing purposes you can only start 4 nodes from the docker-compose.yml in the test folder

```
docker compose up
```


Get chord info:
Ports range from 8881-8889 when starting all nodes
```
GET http://localhost:8881/api/node
```

Send Message to node (Could be done better with JSON but for testing purposes it works):
```
POST http://localhost:8881/api/node/send-message?destinationId=3&message=Hallo du!
```
