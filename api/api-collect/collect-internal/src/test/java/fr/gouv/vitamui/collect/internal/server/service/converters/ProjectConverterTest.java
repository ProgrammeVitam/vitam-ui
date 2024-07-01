/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.internal.server.service.converters;

import fr.gouv.vitam.collect.common.dto.MetadataUnitUp;
import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectMetadataUnitUpDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class ProjectConverterTest {

    private CollectProjectDto collectProjectDto;
    private ProjectDto projectDto;

    private static CollectProjectDto newCollectProjectDto() {
        return CollectProjectDto.builder()
            .tenant(1)
            .archivalAgencyIdentifier("archivalAgencyIdentifier")
            .archivalAgreement("archivalAgreement")
            .archiveProfile("archiveProfile_archivalProfile")
            .comment("comment")
            .id("ID")
            .messageIdentifier("messageIdentifier")
            .unitUp("unitUp")
            .unitUps(List.of(newCollectMetadataUnitUpDto("unit 1"), newCollectMetadataUnitUpDto("unit 2")))
            .originatingAgencyIdentifier("originatingAgencyIdentifier")
            .submissionAgencyIdentifier("submissionAgencyIdentifier")
            .transferringAgencyIdentifier("transferringAgencyIdentifier")
            .legalStatus("legalStatus")
            .acquisitionInformation("acquisitionInformation")
            .createdOn("createdOn_creationDate")
            .lastModifyOn("lastModifyOn_lastUpdate")
            .status("status")
            .name("name")
            .build();
    }

    private static CollectMetadataUnitUpDto newCollectMetadataUnitUpDto(String id) {
        return CollectMetadataUnitUpDto.builder()
            .unitUp(id)
            .metadataKey("metadataKey")
            .metadataValue("metadataValue")
            .build();
    }

    private static ProjectDto newProjectDto() {
        ProjectDto externalDto = new ProjectDto();
        externalDto.setId("ID");
        externalDto.setTenant(1);
        externalDto.setArchivalAgencyIdentifier("archivalAgencyIdentifier");
        externalDto.setArchivalAgreement("archivalAgreement");
        externalDto.setComment("comment");
        externalDto.setArchivalProfile("archiveProfile_archivalProfile");
        externalDto.setMessageIdentifier("messageIdentifier");
        externalDto.setOriginatingAgencyIdentifier("originatingAgencyIdentifier");
        externalDto.setSubmissionAgencyIdentifier("submissionAgencyIdentifier");
        externalDto.setTransferringAgencyIdentifier("transferringAgencyIdentifier");
        externalDto.setUnitUp("unitUp");
        externalDto.setUnitUps(List.of(newMetadataUnitUp("unit 1"), newMetadataUnitUp("unit 2")));
        externalDto.setAcquisitionInformation("acquisitionInformation");
        externalDto.setLegalStatus("legalStatus");
        externalDto.setStatus("status");
        externalDto.setCreationDate("createdOn_creationDate");
        externalDto.setLastUpdate("lastModifyOn_lastUpdate");
        externalDto.setName("name");
        return externalDto;
    }

    private static MetadataUnitUp newMetadataUnitUp(String id) {
        MetadataUnitUp metadataUnitUp = new MetadataUnitUp();
        metadataUnitUp.setUnitUp(id);
        metadataUnitUp.setMetadataKey("metadataKey");
        metadataUnitUp.setMetadataValue("metadataValue");
        return metadataUnitUp;
    }

    @BeforeEach
    void beforeEach() {
        collectProjectDto = newCollectProjectDto();
        projectDto = newProjectDto();
    }

    @Test
    void toVitamuiCollectProjectDtos() {
        Assertions.assertEquals(
            List.of(collectProjectDto),
            ProjectConverter.toVitamuiCollectProjectDtos(List.of(projectDto))
        );
        Assertions.assertEquals(
            Collections.emptyList(),
            ProjectConverter.toVitamuiCollectProjectDtos(Collections.emptyList())
        );
    }

    @Test
    void toVitamuiCollectProjectDto() {
        Assertions.assertEquals(collectProjectDto, ProjectConverter.toVitamuiCollectProjectDto(projectDto));
    }

    @Test
    void toVitamProjectDto() {
        Assertions.assertEquals(projectDto, ProjectConverter.toVitamProjectDto(collectProjectDto));
    }
}
