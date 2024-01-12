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