/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2026)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 */
package fr.gouv.vitamui.commons.api.download;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DownloadClaims {

    private String resource;
    private String subject;
    private Integer tenantId;
    private String accessContractId;
    private String applicationSessionId;
    private long issuedAt;
    private long expiresAt;
    private Map<String, String> parameters = new HashMap<>();
}
