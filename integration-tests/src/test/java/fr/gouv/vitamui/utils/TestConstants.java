package fr.gouv.vitamui.utils;

import fr.gouv.vitamui.cucumber.common.BaseIntegration;

/**
 * Test Constants.
 *
 *
 */
public class TestConstants {

    public static final String SYSTEM_USER_PREFIX_EMAIL = "admin";

    public static final String JULIEN_USER_PREFIX_EMAIL = "julien";

    public static final String JULIEN_USER_ID = "vitamuijulien";

    public static final String SYSTEM_CUSTOMER_ID = "system_customer";

    public static final String CLIENT1_CUSTOMER_ID = "5c7927af7884583d1ebb6e7a74547a15e35d431599d976a9708eb12d6c5e56c9";

    public static final String SYSTEM_CUSTOMER_CODE = "000000";

    public static final String VITAM_UI_CUSTOMER_CODE = "123456";

    /**
     * This value is defined now in {@link BaseIntegration} and is read from config file.
     * It will be removed with the next version.
     */
    @Deprecated
    public static final int SYSTEM_ARCHIVE_TENANT_IDENTIFIER = 9;

    /**
     * This value is defined now in {@link BaseIntegration} and is read from config file.
     * It will be removed with the next version.
     */
    @Deprecated
    public static final int SYSTEM_TENANT_IDENTIFIER = 10;

    /**
     * This value is defined now in {@link BaseIntegration} and is read from config file.
     * It will be removed with the next version.
     */
    @Deprecated
    public static final int CAS_TENANT_IDENTIFIER = 11;

    /**
     * This value is defined now in {@link BaseIntegration} and is read from config file.
     * It will be removed with the next version.
     */
    @Deprecated
    public static final int CLIENT1_TENANT_IDENTIFIER = 102;

    public static final String TOKEN_USER_CAS = "tokcas_ie6UZsEcHIWrfv2x";

    public static final String TOKEN_USER_ADMIN = "tokenadmin";

    public static final String UPDATED = "UPDATED: ";

    public static final String ADMIN_GROUP_NAME = "Groupe de l'administrateur VitamUI";

    public static final String ADMIN_GROUP_ID = "admin_group";

    public static final String SYSTEM_USER_PROFILE_ID = "system_user_profile";

    public static final String SYSTEM_USER_PROFILE_NAME = "User Profile";

    public static final String ADMIN_LEVEL = "";

    public static final String TESTS_PROFILE_ID = "integration-tests_profile";

    public static final String TESTS_GROUP_ID = "integration-tests_group";

    public static final String TESTS_USER_ID = "integration-tests_user";

    public static final String TESTS_TOKEN_ID = "integrationteststokenuser";

    public static final String BAD_LOGIN = "badLogin";

    public static final String SYSTEM_TENANT_ID = "system_tenant";

    public static final String CAS_TENANT_ID = "cas_tenant";

    public static final String CAS_TENANT_NAME = "Tenant CAS";

    public static final String SYSTEM_OWNER_ID = "system_owner";

    public static final String SYSTEM_OWNER_CODE = "000001";

    public static final String SYSTEM_IDP_ID = "system_idp";

    public static final String FAKE_USER_EMAIL = "noexistinguser@notfound.com";

    public static final String NEW_ROLE = "NEW_ROLE";

    public static final String EVENT_DATE_TIME_KEY = "Date d'opération";

    public static final String PIERRE_USER_PREFIX_EMAIL = "pierre";

    public static final String CONTEXT_ID = "CONTEXT_ID";

    public static final String CONTEXT_IDENTIFIER = "CT-000001";

    public static final String CONTEXT_NAME = "admin-context";

    public static final String ACCESS_CONTRACT_ID = "ACCESS_CONTRACT_ID";

    public static final String ACCESS_CONTRACT_IDENTIFIER = "IC-000001";

    public static final String ACCESS_CONTRACT_NAME = "IC-000001";

    public static final String INGEST_CONTRACT_ID = "INGEST_CONTRACT_ID";

    public static final String INGEST_CONTRACT_IDENTIFIER = "ArchivalAgreement0";

    public static final String INGEST_CONTRACT_NAME = "ArchivalAgreement0";

    public static final String SECURITY_PROFILE_ID = "SECURITY_PROFILE_ID";

    public static final String SECURITY_PROFILE_IDENTIFIER = "admin-security-profile";

    public static final String SECURITY_PROFILE_NAME = "admin-security-profile";


    private TestConstants() {
        // do nothing
    }

}
