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
    generateServerCertAndStorePassphrase          vitamui-services security

    # Server and Client for https or mTLS
    generateServerAndClientCertAndStorePassphrase vitamui-services iam
    generateServerAndClientCertAndStorePassphrase vitamui-services referential
    generateServerAndClientCertAndStorePassphrase vitamui-services cas-server
    generateServerAndClientCertAndStorePassphrase vitamui-services ingest
    generateServerAndClientCertAndStorePassphrase vitamui-services archive-search
    generateServerAndClientCertAndStorePassphrase vitamui-services collect
    generateServerAndClientCertAndStorePassphrase vitamui-services pastis
    generateServerAndClientCertAndStorePassphrase vitamui-services api-gateway

    # Zone UI - Client Only for mTLS
    generateClientCertAndStorePassphrase          vitamui-services ui-portal
    generateClientCertAndStorePassphrase          vitamui-services ui-identity
    generateClientCertAndStorePassphrase          vitamui-services ui-identity-admin
    generateClientCertAndStorePassphrase          vitamui-services ui-referential
    generateClientCertAndStorePassphrase          vitamui-services ui-ingest
    generateClientCertAndStorePassphrase          vitamui-services ui-archive-search
    generateClientCertAndStorePassphrase          vitamui-services ui-collect
    generateClientCertAndStorePassphrase          vitamui-services ui-pastis

    # Reverse - Server Only for https
    generateServerCertAndStorePassphrase          vitamui-services reverse

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase          client-vitam     vitamui

    # Example of generated client cert for a customer allowing to perform request on external APIs
    # generateClientCertAndStorePassphrase          client-external customer_x
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
