#!/bin/bash
# stop-elastic-search-cluster.sh

docker-compose -f common.yml -f elastic_cluster.yml down