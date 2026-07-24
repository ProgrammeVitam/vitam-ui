/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

/**
 * Constants shared between the subrogation login flow and the {@code OpaqueVitamTokenGenerator}.
 * The {@link #SUBROGATED_AUTHORITY} marker is added to the {@code Authentication.authorities} when a
 * subrogation-authenticated session is established, so the token generator can request an opaque
 * {@code TOK-<UUID>} with {@code surrogation=true} in Mongo.
 */
public final class SubrogationConstants {

    public static final String SUBROGATED_AUTHORITY = "SUBROGATED";

    public static final String PARAM_SUPER_USER_EMAIL = "superUserEmail";
    public static final String PARAM_SUPER_USER_CUSTOMER_ID = "superUserCustomerId";
    public static final String PARAM_SURROGATE_EMAIL = "surrogateEmail";
    public static final String PARAM_SURROGATE_CUSTOMER_ID = "surrogateCustomerId";

    private SubrogationConstants() {}
}
