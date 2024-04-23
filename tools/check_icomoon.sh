#!/bin/bash

# This script is designed to extract icons used in code and icons from icomoon-selection.json, compare them and alert if some icons are missing from icomoon or are present but not used in the code. It excludes usage from starter-kit.

cd "$(dirname "$0")"

RED='\033[0;31m'
ORANGE='\033[0;33m'
NC='\033[0m' # No Color

ICONS_IN_CODE=$(grep --include='*.'{html,ts} --exclude-dir='starter-kit' -REoha "vitamui-icon-[^\"'< ]+" ../ui/* | sed -e 's/vitamui-icon-//' | sort | uniq)
ICONS_IN_ICOMOON=$(jq '.icons[].properties.name' ../ui/ui-frontend-common/icomoon-selection.json | sed -e 's/"//g' | sort | uniq)

IN_ICOMOON_BUT_NOT_USED_IN_CODE=$(comm -13 <(echo "$ICONS_IN_CODE") <(echo "$ICONS_IN_ICOMOON"))
if [ ! -z "$IN_ICOMOON_BUT_NOT_USED_IN_CODE" ]; then
    echo -e "${ORANGE}Icons in icomoon but not used in code:${NC}"
    echo "$IN_ICOMOON_BUT_NOT_USED_IN_CODE" | while IFS= read -r line ; do echo -e "${ORANGE} - $line${NC}"; done
fi

USED_IN_CODE_BUT_NOT_IN_ICOMOON=$(comm -23 <(echo "$ICONS_IN_CODE") <(echo "$ICONS_IN_ICOMOON"))
if [ ! -z "$USED_IN_CODE_BUT_NOT_IN_ICOMOON" ]; then
    echo -e "${RED}Icons used in code but missing in icomoon:${NC}"
    echo "$USED_IN_CODE_BUT_NOT_IN_ICOMOON" | while IFS= read -r line ; do echo -e "${RED} - $line${NC}"; done
    exit 1 # Missing icons is an error
fi
