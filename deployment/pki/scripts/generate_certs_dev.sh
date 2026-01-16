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
    generateServerAndClientCertAndStorePassphrase   ui-portal           vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-identity         vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-identity-admin   vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-referential      vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-ingest           vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-archive-search   vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-pastis           vitamui-services
    generateServerAndClientCertAndStorePassphrase   ui-collect          vitamui-services
    generateServerCertAndStorePassphrase            ui-design-system    vitamui-services

    #Reverse
    generateServerCertAndStorePassphrase            reverse             hosts_vitamui_reverseproxy     vitamui-services

    # Example of generated client cert for a customer allowing to perform request on external APIs
    generateClientCertAndStorePassphrase            customer_x          client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase            vitamui             client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
