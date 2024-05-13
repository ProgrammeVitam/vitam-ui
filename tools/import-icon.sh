#!/bin/bash

cd "$(dirname "$0")"

if [ $# -lt 1 ]; then
    echo "Usage: $0 <path-to-icomoon-folder-or-zip>"
    exit 1
fi

folder=$1

if [[ "$folder" == *.zip ]]; then
    echo "Unzipping in temporary directory..."
    tmpdir=$(mktemp -d)
    unzip -qq "$folder" -d "$tmpdir"
    folder=$tmpdir
else
    if [ ! -d "$folder" ]; then
        echo "Directory \"$folder\" DOES NOT exists."
        exit 1
    fi
fi

LIBRARY_PATH="../ui/ui-frontend/projects/vitamui-library"
CAS_PATH="../cas/cas-server"

cp -r "$folder/fonts" "$LIBRARY_PATH/src/sass/icons" &&
echo "Replaced font files SUCCESS"

cp "$folder/selection.json" "$LIBRARY_PATH/icomoon-selection.json" &&
echo "Replaced icomoon-selection.json SUCCESS"

cp "$folder/style.css" "$LIBRARY_PATH/src/sass/icons/vitamui-icons.css" &&
sed -i 's/i {/i.vitamui-icon {/1' "$LIBRARY_PATH/src/sass/icons/vitamui-icons.css" &&
echo "Updated vitamui-icon.css SUCCESS"

cp -r "$LIBRARY_PATH/src/sass/icons" "$CAS_PATH/src/main/resources/static" &&
echo "Copied icons to CAS"

echo "DONE"
