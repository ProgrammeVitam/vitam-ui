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
 * Response for {@link CreateTokenRequestDto}: the opaque token id (TOK-&lt;UUID&gt;) usable as a Bearer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTokenResponseDto {

    private String tokenId;
}
