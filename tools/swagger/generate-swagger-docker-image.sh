#!/bin/bash

set -e

# Author : Xelians DevOps
# Purpose : generate the dockerfile of swagger-ui
# Usage : execute the script with the tag of the docker image as argument
function usage {
	echo "usage: echo $0 options"
	echo -e "Options:"
	echo -e "\t Optionnal:"
    echo -e "\t\t --image \t\t\t Name of the image (default: swagger/swagger-ui-dlab:local)"
}

function main {
    POSITIONAL=()
    IMAGE="swagger/swagger-ui-dlab:local"

    while [[ $# -gt 0 ]]
	do
		key="$1"
		case $key in
			--image)
			IMAGE="$2"
			shift
			shift
			;;
			*)    # unknown option
			POSITIONAL+=("$1") # save it in an array for later
			shift
			;;
		esac
	done

    if [[ -z "$IMAGE" ]]
	then
		usage
	else

		SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
        input="$SCRIPT_DIR/tmp/input.txt"
        URLS=()


        mkdir -p $SCRIPT_DIR/tmp        
        find ./ -name swagger.json | awk -F '/swagger/' '{print $NF}' > $input

        export URLS_PRIMARY_NAME=$(basename $(dirname $(head -n 1 $input)))
        

        while read -r line
        do
            mainfolder=$(basename $(dirname $line))
            URLS+=("{ url: '$line', name: '$mainfolder'}")
        done < "$input"

        URLS=$(printf ",%s" "${URLS[@]}")
        export URLS_CONFIG=${URLS:1}

        # Inject variables into the template
        cat Dockerfile.template | envsubst > $SCRIPT_DIR/tmp/Dockerfile
        docker build -f $SCRIPT_DIR/tmp/Dockerfile -t ${IMAGE} . 

        rm -Rf $SCRIPT_DIR/tmp
        
    fi

}
main "$@"