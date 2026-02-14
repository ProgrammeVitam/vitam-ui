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

function getComponentCertificateCn {
    echo "dev.vitamui.com"
}

function getComponentCertificateSan {
    echo "DNS:dev.vitamui.com,DNS:localhost"
}

function generateCerts {

    # Copy CA
    pki_logger "Recopie des clés publiques des CA"
    copyCAFromPki client-external
    copyCAFromPki client-vitam
    copyCAFromPki vitamui-services

    # Generate hosts certificates
    pki_logger "Génération des certificats serveurs"
    # Zone interne
    generateServerCertAndStorePassphrase            security            vitamui-services

    #Zone externe
    generateServerAndClientCertAndStorePassphrase   iam                 vitamui-services
    generateServerAndClientCertAndStorePassphrase   cas-server          vitamui-services
    generateServerAndClientCertAndStorePassphrase   referential         vitamui-services
    generateServerAndClientCertAndStorePassphrase   ingest              vitamui-services
    generateServerAndClientCertAndStorePassphrase   archive-search      vitamui-services
    generateServerAndClientCertAndStorePassphrase   collect             vitamui-services
    generateServerAndClientCertAndStorePassphrase   pastis              vitamui-services
    generateServerAndClientCertAndStorePassphrase   api-gateway         vitamui-services

    #Zone UI
    generateClientCertAndStorePassphrase            ui-portal           vitamui-services
    generateClientCertAndStorePassphrase            ui-identity         vitamui-services
    generateClientCertAndStorePassphrase            ui-identity-admin   vitamui-services
    generateClientCertAndStorePassphrase            ui-referential      vitamui-services
    generateClientCertAndStorePassphrase            ui-ingest           vitamui-services
    generateClientCertAndStorePassphrase            ui-archive-search   vitamui-services
    generateClientCertAndStorePassphrase            ui-pastis           vitamui-services
    generateClientCertAndStorePassphrase            ui-collect          vitamui-services

    #Reverse
    generateServerCertAndStorePassphrase            reverse             vitamui-services

    # Example of generated client cert for a customer allowing to perform request on external APIs
    generateClientCertAndStorePassphrase            customer_x          client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase            vitamui             client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
