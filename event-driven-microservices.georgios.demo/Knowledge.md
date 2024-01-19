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

# Elastic search

## Firing up docker containers
Elastic search has some bootstrap checks, which are executed before starting the clusters. One requirement has to do with vm.max_map_count being at least 262144. I have added some logic to the script which starts the elastic search cluster, to ensure the user knows that and changes the value.

## Snippet body of a POST request to create a new index
```json
{
    "mappings": {
        "properties": {
            "userId": {
                "type": "long"
            },
            "id": {
                "type": "text",
                "fields": {
                    "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                    }
                }
            },
            "createdAt": {
                "type": "date",
		"format": "yyyy-MM-dd'T'HH:mm:ssZZ"
            },
            "text": {
                "type": "text",
                "fields": {
                    "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                    }
                }
            }
        }
    }
}
```
The above has also been added as an actual request in my Postman account under the udemy collection.

## Swapping
Since swapping can severely damage the performance of ES, it is recommended that in distributes set ups where ES is the 
only process in the instance, to disable swapping:
https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration-memory.html

# Important Concepts
- Quorum (Kafka, Elastic search)
  - Set minimum Number of nodes required to create a network and avoid "split brain".  (Lections: 36)
- Configuration as a Service
- Inverted index (Elastic search)