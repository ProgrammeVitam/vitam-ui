#!/bin/bash

if [ $# -lt 1 ]
then
    echo "Usage: $0 <path-to-icomoon-folder>"
    exit 1
fi

folder=$1

if [ ! -d "$folder" ]
then
    echo "Directory \"$folder\" DOES NOT exists." 
    exit 1
fi

cp -r "$folder/fonts" ./src/sass/icons &&
echo "Replaced font files SUCCESS"

cp "$folder/selection.json" ./icomoon-selection.json &&
echo "Replaced icomoon-selection.json SUCCESS"

cp "$folder/style.css" ./src/sass/icons/vitamui-icons.css &&
sed -i 's/i {/i.vitamui-icon {/1' ./src/sass/icons/vitamui-icons.css &&
echo "Updated vitamui-icon.css SUCCESS"

echo "DONE"