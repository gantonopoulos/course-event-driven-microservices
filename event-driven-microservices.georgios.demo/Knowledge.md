# Starting the kafka cluster using docker-compose in background

```
docker-compose -f common.yml -f kafka_cluster.yml up -d
```

# Inspect the cluster

```
kcat -L -b localhost:19092
```

