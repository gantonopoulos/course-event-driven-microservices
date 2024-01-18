#!/bin/bash
# start-kafka-cluster.sh
docker-compose -f common.yml -f kafka_cluster.yml up -d