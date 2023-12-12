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
    generateHostCertAndStorePassphrase          security-internal   hosts_vitamui_security_internal
    generateHostCertAndStorePassphrase          iam-internal        hosts_vitamui_iam_internal
    generateHostCertAndStorePassphrase          referential-internal        hosts_vitamui_referential_internal
    generateHostCertAndStorePassphrase          ingest-internal     hosts_vitamui_ingest_internal
    generateHostCertAndStorePassphrase          archive-search-internal     hosts_vitamui_archive_search_internal
    generateHostCertAndStorePassphrase          collect-internal     hosts_vitamui_collect_internal
    generateHostCertAndStorePassphrase          api-gateway         hosts_vitamui_api_gateway

    #Zone externe
    generateHostCertAndStorePassphrase          iam-external        hosts_vitamui_iam_external
    generateHostCertAndStorePassphrase          referential-external        hosts_vitamui_referential_external
    generateHostCertAndStorePassphrase          cas-server          hosts_cas_server
    generateHostCertAndStorePassphrase          ingest-external     hosts_vitamui_ingest_external
    generateHostCertAndStorePassphrase          archive-search-external     hosts_vitamui_archive_search_external
    generateHostCertAndStorePassphrase          collect-external     hosts_vitamui_collect_external
    generateHostCertAndStorePassphrase          pastis-external     hosts_vitamui_pastis_external

    #Zone UI
    generateClientCertAndStorePassphrase        ui-portal           client-external #hosts_ui_portal
    generateClientCertAndStorePassphrase        ui-identity         client-external #hosts_ui_identity
    generateClientCertAndStorePassphrase        ui-identity-admin   client-external #hosts_ui_identity_admin
    generateClientCertAndStorePassphrase        ui-referential      client-external #hosts_ui_referential
    generateHostCertAndStorePassphrase          ui-ingest           hosts_ui_ingest
    generateHostCertAndStorePassphrase          ui-archive-search   hosts_ui_archive_search
    generateHostCertAndStorePassphrase          ui-collect          hosts_ui_collect
    generateHostCertAndStorePassphrase          ui-pastis           hosts_ui_pastis
    #Reverse
    generateHostCertAndStorePassphrase          reverse             hosts_vitamui_reverseproxy

    # Example of generated client cert for a customer allowing to perform request on external APIs
    generateClientCertAndStorePassphrase        customer_x          client-external

    # Generate Vitam certificates for VitamUI
    generateClientCertAndStorePassphrase        vitamui             client-vitam
}

######################################################################
#############################    Main    #############################
######################################################################

main "$@"
