#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. $(dirname $0)/lib/ca.sh

######################################################################
#########################    Overriding    ###########################
######################################################################

REPERTOIRE_ROOT="$( cd "$( readlink -f $(dirname ${BASH_SOURCE[0]}) )/../../../dev-deployment" ; pwd )"

function get_autorities() {
    echo "server client-external client-vitam"
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
