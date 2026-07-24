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

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Just-in-Time provisioning payload used by the auth-server (SAS POC) after a successful federated
 * authentication (OIDC or SAML). IAM creates a new user in the {@code defaultGroupId} of the
 * {@link fr.gouv.vitamui.iam.common.dto.IdentityProviderDto} referenced by {@code identityProviderId}.
 *
 * <p>Only used when the identity provider has {@code autoProvisioningEnabled = true} and a
 * {@code defaultGroupId} configured.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JitProvisionRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    private String customerId;

    @NotBlank
    private String identityProviderId;

    /** Subject id (unique per IdP) — used as the vitam-ui user technical identifier. */
    @NotBlank
    private String subjectId;

    private String firstname;

    private String lastname;
}
