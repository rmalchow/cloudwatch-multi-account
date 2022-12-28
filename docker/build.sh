#!/bin/bash
set -e

##########################
#
# this needs some variables:
#
DOCKER_REGISTRY=${DOCKER_REGISTRY:-my.docker.registry}
DOCKER_REGISTRY_PATH=${DOCKER_REGISTRY_PATH:-/my-path}
DOCKER_USER=${DOCKER_PASSWORD:-foo}
DOCKER_PASSWORD=${DOCKER_PASSWORD:-bar}
##########################


IMG=${DOCKER_REGISTRY}${DOCKER_REGISTRY_PATH}/cloudwatch-multi-exporter:latest

docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD} ${DOCKER_REGISTRY}
docker build -t ${IMG} .
docker push ${IMG}
