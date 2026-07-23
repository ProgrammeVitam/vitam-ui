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

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to mint an opaque auth token (TOK-&lt;UUID&gt;) pointing to an existing user.
 * Used by external issuers (Spring Authorization Server POC) that need to persist a token
 * in the {@code tokens} collection without going through the standard login flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTokenRequestDto {

    @NotNull
    private String refId;

    private boolean surrogation;

    private boolean api;
}
