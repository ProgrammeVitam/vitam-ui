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
 * Payload used by the auth-server (SAS POC) to validate that an ACCEPTED Subrogation exists between
 * the {@code superUser} and the {@code surrogate} for the given customer contexts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubrogationValidateRequestDto {

    @NotBlank
    private String superUserEmail;

    @NotBlank
    private String superUserCustomerId;

    @NotBlank
    private String surrogateEmail;

    @NotBlank
    private String surrogateCustomerId;
}
