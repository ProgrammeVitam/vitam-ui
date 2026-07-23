/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import lombok.Value;

/** Carries the {@code customerId} resolved by the mini HRD, attached as details of the {@code Authentication}. */
@Value
public class CustomerIdAuthenticationDetails {
    String customerId;
}
