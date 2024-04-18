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

    # Copy CA
    pki_logger "Recopie des clés publiques des CA"
    copyCAFromPki client-external
    copyCAFromPki client-vitam
    copyCAFromPki server

    # Generate hosts certificates
    pki_logger "Génération des certificats serveurs"
    # Zone interne
    generateHostCertAndStorePassphrase   security-internal       hosts_vitamui_security_internal
    generateHostCertAndStorePassphrase   iam-internal            hosts_vitamui_iam_internal
    generateHostCertAndStorePassphrase   referential-internal    hosts_vitamui_referential_internal
    generateHostCertAndStorePassphrase   ingest-internal         hosts_vitamui_ingest_internal
    generateHostCertAndStorePassphrase   archive-search-internal hosts_vitamui_archive_search_internal
    generateHostCertAndStorePassphrase   collect-internal        hosts_vitamui_collect_internal
    generateHostCertAndStorePassphrase   api-gateway             hosts_vitamui_api_gateway

    #Zone externe
    generateHostCertAndStorePassphrase   iam-external            hosts_vitamui_iam_external
    generateHostCertAndStorePassphrase   referential-external    hosts_vitamui_referential_external
    generateHostCertAndStorePassphrase   cas-server              hosts_cas_server
    generateHostCertAndStorePassphrase   ingest-external         hosts_vitamui_ingest_external
    generateHostCertAndStorePassphrase   archive-search-external hosts_vitamui_archive_search_external
    generateHostCertAndStorePassphrase   collect-external        hosts_vitamui_collect_external
    generateHostCertAndStorePassphrase   pastis-external         hosts_vitamui_pastis_external

    #Zone UI
    generateClientCertAndStorePassphrase ui-portal               client-external
    generateClientCertAndStorePassphrase ui-identity             client-external
    generateClientCertAndStorePassphrase ui-identity-admin       client-external
    generateClientCertAndStorePassphrase ui-referential          client-external
    generateClientCertAndStorePassphrase ui-ingest               client-external
    generateClientCertAndStorePassphrase ui-archive-search       client-external
    generateClientCertAndStorePassphrase ui-collect              client-external
    generateClientCertAndStorePassphrase ui-pastis               client-external

    #Reverse
    generateHostCertAndStorePassphrase   reverse                 hosts_vitamui_reverseproxy

    # Example of generated client cert for a customer allowing to perform request on external APIs
    # generateClientCertAndStorePassphrase customer_x              client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase vitamui                 client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
