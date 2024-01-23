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

## Searching an index
To search an index, we can sent a GET request (e.g. using Postman) to localhost:9200/twitter-index/**_search** endpoint.

## Swapping
Since swapping can severely damage the performance of ES, it is recommended that in distributes set ups where ES is the 
only process in the instance, to disable swapping:
https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration-memory.html

## Adding an index

On occasion, for testing purposes, we need to insert some entries to the elastic search index. To do so, the elastic cluster must be running and then we can use postman to issue a POST command.

### Starting the cluster:
```
./start-elastic-search-cluster.sh
```

### Issuing POST command
**Endpoint:**  twitter-index/_doc/1​

**Body:**
```json
{
    "userId": "1",
    "id": "1",
    "createdAt": "2020-01-03T23:00:50+0000",
    "text": "test multi word"
}
```

To add more entries, increase the index at the end of the endpoint.

NOTE: Elastic search, at least for now, allows only one index mapping. This is why why are using "_doc" to access it, but we can ommit it if we want.

## Quering the index

The following are some endpoints we can append to the elastic clustter's address to issue commands which query the elastic index:


```http
GET twitter-index/_search
```
Get all data ( Max 10000 records – use scroll Api to get more )​

```http
GET twitter-index/_search?size=2000
```
Specify size in query​

```http
GET twitter-index/_search?q=id:1
```
Get data with id=1​

```http
GET twitter-index/_search?q=text:test
```
Get data with text=test​

NOTE: No need to specify “_doc” mapping in query as current elasticsearch only allows one mapping.​

## Passing queries as JSON
It is possible to create a query in a JSON format using the ElasticSearch's query language. 
We do this by sending a POST request to **twitter-index/_search​**
using as **Body** the JSON query. E.g, to get all index entries with key **text** containing the word "test", we issue the following POST command:

```http
POST twitter-index/_search​
```

```json
{
    "query": {
        "term": {
            "text": "test"
        }
    }
}
```

### Type of queries supported

#### Match Query
Uses every word in the input query, analyzes them and returns combinations of those words:

```json
{
    "query": {
        "match": {
            "text": "test multi word"
        }
    }
}
```
The above will return all records that contain one or more instances of the words "test", "multi", "word".

#### Term Query
By filling in the "keyword" property of **text** we can skip all analysing and ask ES to return any a entries that exactly contain the input text:

```json
{
    "query": {
        "term": {
            "text.keyword": "test multi word"
        }
    }
}
```

#### Wildcard Queries
Will return all entries that contain a word that matchs the wildcard:

```json
{
    "query": {
        "wildcard": {
            "text": "te*"
        }
    }
}
```
The query above will return all entries that contain any word which starts with "te" or "te" itself (* => 0 or more).
We can define more complex wildcards.

#### Query string
It analyzes the input, unlike wildcard, and can accept multiple wildcards in the query field:

```json
{
    "query": {
        "query_string": {
            "query": "text:te*"
        }
    }
}
```

### Complex Queries
We can combine the above types to create complex queries. We can use terms like "should" for **OR** conditionals or "must" for **AND**. e.g:

```json
{
    "from": 0,
    "size": 20,
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "text": "test"
                    }
                },
                {
                    "match": {
                        "text": "word"
                    }
                }
            ]
        }
    }
}
```

# Important Concepts
- Quorum (Kafka, Elastic search)
  - Set minimum Number of nodes required to create a network and avoid "split brain".  (Lections: 36)
- Configuration as a Service
- Inverted index (Elastic search)