#!/usr/bin/env bash
set -e

######################################################################
############################# Includes  ##############################
######################################################################

. "$(dirname $0)/lib/certs.sh"

######################################################################
#########################    Overriding    ###########################
######################################################################

function generateCerts {

    pki_logger "Copying CA certificates"
    for AUTHORITY_NAME in $(get_autorities); do
        copyCAFromPki "${AUTHORITY_NAME}"
    done

    # VitamUI Services
    # Server Only for https
    generateServerCertAndStorePassphrase            security            vitamui-services

    # Server and Client for https or mTLS
    generateServerAndClientCertAndStorePassphrase   iam                 vitamui-services
    generateServerAndClientCertAndStorePassphrase   referential         vitamui-services
    generateServerAndClientCertAndStorePassphrase   cas-server          vitamui-services
    generateServerAndClientCertAndStorePassphrase   ingest              vitamui-services
    generateServerAndClientCertAndStorePassphrase   archive-search      vitamui-services
    generateServerAndClientCertAndStorePassphrase   collect             vitamui-services
    generateServerAndClientCertAndStorePassphrase   pastis              vitamui-services
    generateServerAndClientCertAndStorePassphrase   api-gateway         vitamui-services

    # Zone UI - Client Only for mTLS
    generateClientCertAndStorePassphrase            ui-portal           vitamui-services
    generateClientCertAndStorePassphrase            ui-identity         vitamui-services
    generateClientCertAndStorePassphrase            ui-identity-admin   vitamui-services
    generateClientCertAndStorePassphrase            ui-referential      vitamui-services
    generateClientCertAndStorePassphrase            ui-ingest           vitamui-services
    generateClientCertAndStorePassphrase            ui-archive-search   vitamui-services
    generateClientCertAndStorePassphrase            ui-collect          vitamui-services
    generateClientCertAndStorePassphrase            ui-pastis           vitamui-services

    # Reverse - Server Only for https
    generateServerCertAndStorePassphrase            reverse             vitamui-services

    # Example of generated client cert for a customer allowing to perform request on external APIs
    # generateClientCertAndStorePassphrase customer_x              client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase            vitamui             client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
