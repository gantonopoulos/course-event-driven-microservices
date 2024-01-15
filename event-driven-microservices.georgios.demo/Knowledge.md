# Starting the kafka cluster using docker-compose in background

```
docker-compose -f common.yml -f kafka_cluster.yml up -d
```

# Inspect the cluster

```
kcat -L -b localhost:19092
```
# Building without unit tests
This can be helpful when trying to build an image because for the unit tests to run, we need to have a cluster running.
```
mvn clean install -DskipTests
```

# Checking the logs in the containers

```
docker logs CONTAINER_ID
```

# Starting the twitter-to-kafka-service after we have introduced independent Configuration Server

## Running locally (non-containerised)

1) First we need to have the Kafka cluster up and running. The cluster can run in the container, using the docker-compose
command without the service container (we want to start twitter-to-kafka-service locally).

2) Next we need to run the ConfigServer application, because the twitter-to-kafka-service will try to access it to read the configuration values and populate its configuration model. We can start the ConfigServer from the IDE (there exists a build configuration).
3) Now we can run the twitter-to-kafka-service from the IDE as well (another build configuration). We should see that the service is accessing http://localhost:8888 for configuration, which is where the ConfigServer is running.