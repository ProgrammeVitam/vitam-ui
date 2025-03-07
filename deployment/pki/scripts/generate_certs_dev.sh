#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/certs.sh"

######################################################################
#########################    Overriding    ###########################
######################################################################

REPERTOIRE_ROOT="$( cd "$( readlink -f $(dirname ${BASH_SOURCE[0]}) )/../../../dev-deployment" ; pwd )"

function getHostCertificateCn {
    echo "dev.vitamui.com"
}

function getHostCertificateSan {
    echo "DNS:dev.vitamui.com,DNS:localhost"
}

function generateCerts {

    # Copy CA
    pki_logger "Recopie des clés publiques des CA"
    copyCAFromPki client-external
    copyCAFromPki client-vitam
    copyCAFromPki server

    # Zone API
    pki_logger "Génération des certificats serveurs"
    generateHostCertAndStorePassphrase   security                hosts_vitamui_security
    generateHostCertAndStorePassphrase   api-gateway             hosts_vitamui_api_gateway
    generateHostCertAndStorePassphrase   iam                     hosts_vitamui_iam
    generateHostCertAndStorePassphrase   referential             hosts_vitamui_referential
    generateHostCertAndStorePassphrase   cas-server              hosts_cas_server
    generateHostCertAndStorePassphrase   ingest                  hosts_vitamui_ingest
    generateHostCertAndStorePassphrase   archive-search          hosts_vitamui_archive_search
    generateHostCertAndStorePassphrase   collect                 hosts_vitamui_collect
    generateHostCertAndStorePassphrase   pastis                  hosts_vitamui_pastis

    #Zone UI
    pki_logger "Génération des certificats clients UI"
    generateClientCertAndStorePassphrase ui-portal               client-external
    generateClientCertAndStorePassphrase ui-identity             client-external
    generateClientCertAndStorePassphrase ui-identity-admin       client-external
    generateClientCertAndStorePassphrase ui-referential          client-external
    generateClientCertAndStorePassphrase ui-ingest               client-external
    generateClientCertAndStorePassphrase ui-archive-search       client-external
    generateClientCertAndStorePassphrase ui-collect              client-external
    generateClientCertAndStorePassphrase ui-pastis               client-external
    generateClientCertAndStorePassphrase ui-design-system        client-external

    #Reverse
    generateHostCertAndStorePassphrase   reverse                 hosts_vitamui_reverseproxy

    # Example of generated client cert for a customer allowing to perform request on APIs
    generateClientCertAndStorePassphrase customer_x              client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase vitamui                 client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
