#!/usr/bin/env bash

set -e

IMAGE_TAG="ontdekstation-server"
VERSION="${1-latest}"

# script takes an optional tag argument, otherwise uses "latest"
docker build \
    -f /var/lib/jenkins/workspace/BackEndTest/ClimateChecker/Dockerfile \
    -t "$IMAGE_TAG:$VERSION" .

echo "$IMAGE_TAG:$VERSION"
