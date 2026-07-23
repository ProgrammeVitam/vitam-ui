/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.
 */
package fr.gouv.vitamui.iam.common.dto.cas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mini Home Realm Discovery entry: the tuple (customer, matching identity provider) resolved for an email.
 * Reproduces the pattern-matching resolution done today by {@code IdentityProviderHelper} inside CAS webflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrdEntryDto {

    private String customerId;

    private String customerName;

    private String identityProviderId;

    private String identityProviderName;

    /** {@code true} if the matched IdP is the local username/password provider (Apereo &quot;internal&quot;). */
    private boolean internal;
}
