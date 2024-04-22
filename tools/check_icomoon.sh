#!/bin/bash

# This script is designed to extract icons used in code and icons from icomoon-selection.json, compare them and alert if some icons are missing from icomoon or are present but not used in the code. It excludes usage from starter-kit.

cd "$(dirname "$0")"

ICONS_IN_CODE=$(grep --include='*.'{html,ts} --exclude-dir='starter-kit' -REoha "vitamui-icon-[^\"'< ]+" ../ui/* | sed -e 's/vitamui-icon-//' | sort | uniq)
ICONS_IN_ICOMOON=$(jq '.icons[].properties.name' ../ui/ui-frontend-common/icomoon-selection.json | sed -e 's/"//g' | sort | uniq)
ICONS_IN_STARTER_KIT=$(grep -Eoha "vitamui-icon-[^\"'< ]+" ../ui/ui-frontend/projects/starter-kit/src/app/components/icons/icons.component.html | sed -e 's/vitamui-icon-//' | sort | uniq)

echo "Icons used in code but missing in icomoon:"
comm -23 <(echo "$ICONS_IN_CODE") <(echo "$ICONS_IN_ICOMOON") | sed -E 's/^/  - /'
echo "Icons in icomoon but not used in code:"
comm -13 <(echo "$ICONS_IN_CODE") <(echo "$ICONS_IN_ICOMOON") | sed -E 's/^/  - /'
