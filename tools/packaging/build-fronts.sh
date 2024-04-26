#!/bin/bash
echo "$VERSION"
echo "$MODULES"


# Check if VERSION and MODULES environment variables are set
if [ -z "$VERSION" ] || [ -z "$MODULES" ]; then
    echo "ERROR: VERSION or MODULES environment variables are not set."
    exit 1
fi

# Split MODULES names into an array
IFS=',' read -r -a MODULES_ARRAY <<< "$MODULES"


# Determine the directory where the script resides
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Check if the Makefile-fronts exists in the same directory as the script
if [ ! -f "$SCRIPT_DIR/Makefile-fronts" ]; then
    echo "ERROR: Makefile-fronts not found in the same directory as the script."
    exit 1
fi

# Change directory to the script directory
cd "$SCRIPT_DIR" || exit 1


for MODULE in "${MODULES_ARRAY[@]}"; do
    echo "Packaging module: $MODULE"
    # Call make passing module and version as parameters
    make -f "Makefile-fronts" NAME="$MODULE" VERSION="$VERSION"
    # If you want to pass additional parameters to the Makefile, add them after VERSION="$VERSION"
done
