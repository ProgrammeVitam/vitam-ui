/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Carries the {@code customerId} resolved by the mini HRD, attached as details of the {@code Authentication}.
 *
 * <p>Kept as a hand-written immutable class (was Lombok {@code @Value}) so Jackson can round-trip it
 * through the persistent {@code OAuth2AuthorizationService} — Lombok's generated constructor lacks the
 * annotations Jackson needs on the target JVM.
 */
public final class CustomerIdAuthenticationDetails {

    private final String customerId;

    @JsonCreator
    public CustomerIdAuthenticationDetails(@JsonProperty("customerId") String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerIdAuthenticationDetails other)) return false;
        return java.util.Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(customerId);
    }

    @Override
    public String toString() {
        return "CustomerIdAuthenticationDetails(customerId=" + customerId + ")";
    }
}
