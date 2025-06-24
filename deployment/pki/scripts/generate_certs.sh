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
    generateHostCertAndStorePassphrase   security                hosts_vitamui_security
    generateHostCertAndStorePassphrase   api-gateway             hosts_vitamui_api_gateway

    #Zone externe
    generateHostCertAndStorePassphrase   iam                     hosts_vitamui_iam
    generateHostCertAndStorePassphrase   referential             hosts_vitamui_referential
    generateHostCertAndStorePassphrase   cas-server              hosts_cas_server
    generateHostCertAndStorePassphrase   ingest                  hosts_vitamui_ingest
    generateHostCertAndStorePassphrase   archive-search          hosts_vitamui_archive_search
    generateHostCertAndStorePassphrase   collect                 hosts_vitamui_collect
    generateHostCertAndStorePassphrase   pastis                  hosts_vitamui_pastis

    #Zone UI
    generateClientCertificate   ui-portal               hosts_ui_portal
    generateClientCertificate   ui-identity             hosts_ui_identity
    generateClientCertificate   ui-identity-admin       hosts_ui_identity_admin
    generateClientCertificate   ui-referential          hosts_ui_referential
    generateClientCertificate   ui-ingest               hosts_ui_ingest
    generateClientCertificate   ui-archive-search       hosts_ui_archive_search
    generateClientCertificate   ui-collect              hosts_ui_collect
    generateClientCertificate   ui-pastis               hosts_ui_pastis
    generateClientCertificate   ui-design-system        hosts_ui_design_system

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
