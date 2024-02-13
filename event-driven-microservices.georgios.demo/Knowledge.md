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

# Encryption

We can encrypt/decrypt any string by starting the **config-server** service locally (from the IDE) and then issuing a post command on Postman. I have saved those commands in a collection in my postman account. Keep in ming that **config-server** is using the ENCRYPT_KEY parameter to encrypt/decrypt inputs, so this must be defined in the .bashrc.

## Intellij
It appears that Intellij cannot read the environment variables from the bashrc (TODO: some configuring must be needed). Which is why we have to define in the build configurartion, a start up environment variable ENCRYPT_KEY with the same value as in .bashrc.
Docker-compose appears to be accessing the .bashrc without issues, so starting those services (e.g. configuration-client) containerized works as expected.

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

## Using Repository bean in Elastic Search 
There is a Repository pattern implementation in Spring (see [TwitterElasticsearchQueryRepository's](./elastic/elastic-query-client/src/main/java/com/microservices/demo/elastic/query/client/repository/TwitterElasticsearchQueryRepository.java) definition). This offers another way of querying data from elastic search, which however cannot be used for more complex queries. It fetches data in bulk (and then one can refine the fetched collection) or performs CRUD operations. In comparison, the **ElasticsearchOperations** (see [TwitterElasticQueryClient](./elastic/elastic-query-client/src/main/java/com/microservices/demo/elastic/query/client/service/impl/TwitterElasticQueryClient.java)) offers an API with which one can formulate complex queries, which are then sent to the client and you get the results back.

# HATEOAS (Hypermedia As The Engine Of Application State)
HATEOAS stipulates that the representations of REST resources should contain also links to the related resources and not only the data itself. This information must be provided by the server dynamically with hypermedia.

## Advantages
- Helps the client to better understand the API and use it easier.
- Reduces the risk of a client breaking due to api changes.

## Example

For a request of all documents on the "documents" endpoint of our elastic query service, we can display the explicit path of each result along with the endpoint hit:

``` json
 {
        "id": "8522739414942840755",
        "userId": 722153625744397953,
        "text": "Lorem ....",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8183/elastic-query-service/documents/8522739414942840755"
            },
            {
                "rel": "documents",
                "href": "http://localhost:8183/elastic-query-service/documents"
            }
        ]
    }
```

# API Versioning

## Versioning by URI

Normally, using this method we need to create a new controller and then version the endpoint. But you can also directly version the endpoints on the method level for simplicity.

In this case we end up with different uris for the same endpoint on different versions. e.g, switch to **version-by-uri** branch, start elastic-cluster, config server and elastic query service and issue in Postman the following two uris:

``` http
localhost:8183/elastic-query-service/documents/v1/8522739414942840755
localhost:8183/elastic-query-service/documents/v2/8522739414942840755
```

## Media type versioning
In this method we define a media type on the Controller level fist (controller class) by adding the **produces** property. In our example we use a media type we have defined (custom vendor media type), named **application/vnd.api.v1+json**:

``` java
@RequestMapping(value = "/documents", produces = "application/vnd.api.v1+json")
```

Thus we version our API with v1. This specific version has to be requested by the client by use of the **Accepts** header. This way we do not have to change the uri. If we have a new version in one of our endpoints for example, we can mark it with **application/vnd.api.*v2*+json**, e.g.:

``` java
@GetMapping(value = "/{id}",  produces = "application/vnd.api.v2+json")
    public @ResponseBody
    ResponseEntity<ElasticQueryServiceResponseModelV2>
    getDocumentByIdV2(@PathVariable @NotEmpty String id) {
        ElasticQueryServiceResponseModelV2 elasticQueryServiceResponseModel = getV2Model(elasticQueryService.getDocumentById(id));
        LOG.debug("Elasticsearch returned document with id {}", id);
        return ResponseEntity.ok(elasticQueryServiceResponseModel);
    }
```

In the Postman, we only have to add a new **Accepts** clause in the headers with the custom media type of the new version in our request to the endpoint.

This way we can also include the change in our documentation and API definition so that our clients can see the new changes.

If the clients on their side want to start using the new API, the can choose to do so by adding the header clause and adjusting their model.

# Important Concepts
- Quorum (Kafka, Elastic search)
  - Set minimum Number of nodes required to create a network and avoid "split brain".  (Lections: 36)
- Configuration as a Service
- Inverted index (Elastic search)
- HATEOAS
- Richardson Maturity Model