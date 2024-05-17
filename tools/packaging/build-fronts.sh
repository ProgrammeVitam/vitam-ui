#!/bin/bash

MODULES="$1"
VERSION="$2"

echo "$MODULES"
echo "$VERSION"


# Check if VERSION and MODULES environment variables are set
if [ -z "$VERSION" ] || [ -z "$MODULES" ]; then
    echo "ERROR: VERSION or MODULES environment variables are not set."
    exit 1
fi

# Split the modules list by comma into an array
IFS=',' read -r -a modules_array <<< "$MODULES"

for MODULE in "${modules_array[@]}"; do
    echo "Packaging module: $MODULE"
    # Call make passing module and version as parameters
    make -f "Makefile-fronts" NAME="$MODULE" VERSION="$VERSION" rpm

done
