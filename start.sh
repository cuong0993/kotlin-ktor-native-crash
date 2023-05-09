#!/bin/bash

ARCH=$(uname -m)
DOCKER_COMPOSE_FILE=${DOCKER_COMPOSE_FILE:='docker-compose.yaml'}
DOCKER_LOG_FILE=${DOCKER_LOG_FILE:='dockerCompose.log'}

if [[ ("$DOCKER_COMPOSE_FILE" == "docker-compose.yml") && (-z "$ARCH" || "$ARCH" == "arm64") ]] ; then
    echo "Building imgage for arm64 as amd since kotlin do not support arm yet"
    COMPOSE_DOCKER_CLI_BUILD=1
    DOCKER_BUILDKIT=1
    DOCKER_DEFAULT_PLATFORM=linux/amd64
fi

if ! command -v docker compose version &> /dev/null; then
 DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
 mkdir -p $DOCKER_CONFIG/cli-plugins
 curl -SL https://github.com/docker/compose/releases/download/v2.17.2/docker-compose-linux-x86_64 -o $DOCKER_CONFIG/cli-plugins/docker-compose
 chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose
 docker compose version
fi

docker compose -f "$DOCKER_COMPOSE_FILE" --project-name "kotlin-ktor-native-crash" up --build --detach --pull "missing"
docker compose -f "$DOCKER_COMPOSE_FILE" --project-name "kotlin-ktor-native-crash" logs --follow --timestamps > "$DOCKER_LOG_FILE" 2>&1 &
