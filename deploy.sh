#!/usr/bin/env bash

echo "    ____             __           "
echo "   / __ \___  ____  / /___  __  __"
echo "  / / / / _ \/ __ \/ / __ \/ / / /"
echo " / /_/ /  __/ /_/ / / /_/ / /_/ / "
echo "/_____/\___/ .___/_/\____/\__, /  "
echo "          /_/            /____/   "

echo BUILDING DOCKER IMAGE
echo "$DOCKERHUB_PASS" | docker login --username vorwahlen --password-stdin
docker build -t backend .
docker tag backend vorwahlen/backend:latest
docker push vorwahlen/backend

echo "Image has been uploaded to Docker Hub."

