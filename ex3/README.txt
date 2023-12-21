In the LoadBalancer Repo:

gradle build
docker build -t load-balancer .
docker run -p 8888:8888 load-balancer -d

In the RESTServer Repo:

gradle build
docker build -t rest-server .
docker run -e LOAD_BALANCER_URL=http://172.17.0.2:8888/api -p 8080:8080 rest-server

Run from docker compose:

build the load-balancer and rest-server docker images and then run: docker compose up