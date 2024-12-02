#!/bin/bash

cd `dirname $0`
cd ..

find_arguments=(
  -type f \( -name "*.scss" -o -name "*.css" -o -name "*.ts" -o -name "*.html" \)
  -not -path "*/dist/*"
  -not -path "*/node_modules/*"
  -not -path "*/target/*"
  -not -path "./docs/*"
  -not -path "./tools/*"
  -not -path "./deployment/*"
  -not -path "*/_elevation.scss"
  -not -path "*/cas.css"
)

red='\033[0;31m'
green='\033[0;32m'
nc='\033[0m' # no color

occurrences_with_filename=$(find . "${find_arguments[@]}" -exec awk '/^[[:space:]]*box-shadow/ {found=1 ; printf "%s", FILENAME ":\t" } found {gsub(/^[[:space:]]+/, ""); printf "%s", $0} /;/ && found {found=0 ; print ""}' {} +)

filenames=$(echo "${occurrences_with_filename}" | cut -d ':' -f 1 | sort | uniq)
canonical_occurrences=$(echo "${occurrences_with_filename}" | cut -d ':' -f 2-| tr '[:upper:]' '[:lower:]' | sed -E 's/ //g' | sort | uniq)

function count_occurrences {
    if [[ -z "${1}" ]]; then
        echo 0
    else
        echo "${1}" | wc -l
    fi
}

nb_occurrences=$(count_occurrences "${occurrences_with_filename}")
nb_different=$(count_occurrences "${canonical_occurrences}")
nb_files=$(count_occurrences "${filenames}")

if [[ $nb_occurrences -gt 0 ]]; then
    echo -e "${red}Found ${nb_occurrences} occurrences of ${nb_different} different values of unexpected shadows in ${nb_files} files:${nc}"
    echo -e "${occurrences_with_filename}"

    exit 1;
else
    echo -e "${green}${context}Found no unexpected occurrence in the code${nc}"
fi

