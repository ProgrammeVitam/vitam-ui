#!/usr/bin/env bash

set -e
cd `dirname $0`

PROJECT_ROOT_DIR="$(readlink -f `dirname $0`/../..)"
TARGET_DIR=target/
DEPLOYMENT_DIR="$TARGET_DIR/deployment/"
REPOSITORY_PACKAGES_DIR="$TARGET_DIR/repository/Packages"
VITAMUI_VERSION=$1
[ -z VITAMUI_VERSION ] && (
    echo "Usage: $0 [VITAMUI_VERSION]";
    exit 1
)

export VITAMUI_VERSION=$VITAMUI_VERSION
mkdir -p $TARGET_DIR $REPOSITORY_PACKAGES_DIR $DEPLOYMENT_DIR

###############################
# Init container local repository
###############################
echo ">>> Prepare packages files for container workspace repository"
echo "-------------------------------------------------------------"
# Find all RPM in project and copy to REPOSITORY_PACKAGES_DIR
find "$PROJECT_ROOT_DIR" -not \( -path $PROJECT_ROOT_DIR/docker/vitamui -prune \) \
     -name "*.rpm" -exec cp -uv {} $REPOSITORY_PACKAGES_DIR \;

###############################
# Init container local deployment files
###############################
echo ">>> Prepare deployment files for container workspace"
echo "-------------------------------------------------------------"
# Copy deployement tar gz to DEPLOYMENT_BUILD_DIR
tar xvzf $PROJECT_ROOT_DIR/deployment/target/deployment-${VITAMUI_VERSION}-ansible.tar.gz -C $DEPLOYMENT_DIR --strip-components=1
# Overwrite deployment with docker_config
echo "Override with vitamui docker specifics values"
cp -R ./deployment-config/*  $DEPLOYMENT_DIR/

###############################
# Init container local deployment files
###############################
echo ">>> Running container to finish repository initialisatiob for vitamui image $VITAMUI_VERSION"
echo "-------------------------------------------------------------"
# Pre build image (provision target dir mainly
docker build . --build-arg VITAMUI_VERSION=$VITAMUI_VERSION -t docker.vitamui.com/vitamui:${VITAMUI_VERSION} --no-cache

###############################
# Start container installation
###############################
echo ">>> Running continaer to finish repository initialisation for vitamui image $VITAMUI_VERSION"
echo "-------------------------------------------------------------"
# Re run image with compose *I.E : systemd service start related workaround for docker container
docker-compose  up -d
# Laucnh install from shipped deployment and repository
docker-compose  exec -T vitamui /workspace/deployment/install.sh -v

###############################
# Save installed image
###############################
# Commit image to publish ir
docker commit vitamui_build docker.vitamui.com/vitamui:${VITAMUI_VERSION}
docker-compose  down
echo "Build success"
