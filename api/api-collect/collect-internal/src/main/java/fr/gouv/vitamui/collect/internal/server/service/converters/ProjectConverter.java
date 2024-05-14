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
 */

package fr.gouv.vitamui.collect.internal.server.service.converters;

import fr.gouv.vitam.collect.common.dto.MetadataUnitUp;
import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectMetadataUnitUpDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class ProjectConverter {

    public static List<CollectProjectDto> toVitamuiCollectProjectDtos(List<ProjectDto> projectDtos) {
        return projectDtos.stream().map(ProjectConverter::toVitamuiCollectProjectDto).collect(Collectors.toList());
    }

    public static CollectProjectDto toVitamuiCollectProjectDto(ProjectDto projectDto) {
        return CollectProjectDto.builder()
            .tenant(Optional.of(projectDto.getTenant()).orElseThrow())
            .archivalAgencyIdentifier(projectDto.getArchivalAgencyIdentifier())
            .archivalAgreement(projectDto.getArchivalAgreement())
            .archiveProfile(projectDto.getArchivalProfile())
            .comment(projectDto.getComment())
            .id(projectDto.getId())
            .messageIdentifier(projectDto.getMessageIdentifier())
            .unitUp(projectDto.getUnitUp())
            .unitUps(toVitamuiCollectMetadataUnitUpDtos(projectDto.getUnitUps()))
            .originatingAgencyIdentifier(projectDto.getOriginatingAgencyIdentifier())
            .submissionAgencyIdentifier(projectDto.getSubmissionAgencyIdentifier())
            .transferringAgencyIdentifier(projectDto.getTransferringAgencyIdentifier())
            .legalStatus(projectDto.getLegalStatus())
            .acquisitionInformation(projectDto.getAcquisitionInformation())
            .createdOn(projectDto.getCreationDate())
            .lastModifyOn(projectDto.getLastUpdate())
            .status(projectDto.getStatus())
            .name(projectDto.getName())
            .automaticIngest(projectDto.getAutomaticIngest())
            .build();
    }

    public static List<CollectMetadataUnitUpDto> toVitamuiCollectMetadataUnitUpDtos(
        List<MetadataUnitUp> metadataUnitUps
    ) {
        if (Objects.isNull(metadataUnitUps)) {
            return null;
        }
        return metadataUnitUps
            .stream()
            .map(ProjectConverter::toVitamuiCollectMetadataUnitUpDto)
            .collect(Collectors.toList());
    }

    public static CollectMetadataUnitUpDto toVitamuiCollectMetadataUnitUpDto(MetadataUnitUp metadataUnitUp) {
        return CollectMetadataUnitUpDto.builder()
            .metadataKey(metadataUnitUp.getMetadataKey())
            .metadataValue(metadataUnitUp.getMetadataValue())
            .unitUp(metadataUnitUp.getUnitUp())
            .build();
    }

    public static ProjectDto toVitamProjectDto(CollectProjectDto collectProjectDto) {
        ProjectDto externalDto = new ProjectDto();
        externalDto.setArchivalAgencyIdentifier(collectProjectDto.getArchivalAgencyIdentifier());
        externalDto.setArchivalAgreement(collectProjectDto.getArchivalAgreement());
        externalDto.setComment(collectProjectDto.getComment());
        externalDto.setArchivalProfile(collectProjectDto.getArchiveProfile());
        externalDto.setId(collectProjectDto.getId());
        externalDto.setMessageIdentifier(collectProjectDto.getMessageIdentifier());
        externalDto.setOriginatingAgencyIdentifier(collectProjectDto.getOriginatingAgencyIdentifier());
        externalDto.setSubmissionAgencyIdentifier(collectProjectDto.getSubmissionAgencyIdentifier());
        externalDto.setTransferringAgencyIdentifier(collectProjectDto.getTransferringAgencyIdentifier());
        externalDto.setUnitUp(collectProjectDto.getUnitUp());
        externalDto.setUnitUps(toVitamMetadataUnitUps(collectProjectDto.getUnitUps()));
        externalDto.setAcquisitionInformation(collectProjectDto.getAcquisitionInformation());
        externalDto.setLegalStatus(collectProjectDto.getLegalStatus());
        externalDto.setStatus(collectProjectDto.getStatus());
        externalDto.setCreationDate(collectProjectDto.getCreatedOn());
        externalDto.setLastUpdate(collectProjectDto.getLastModifyOn());
        externalDto.setName(collectProjectDto.getName());
        externalDto.setAutomaticIngest(collectProjectDto.getAutomaticIngest());
        return externalDto;
    }

    public static List<MetadataUnitUp> toVitamMetadataUnitUps(
        List<CollectMetadataUnitUpDto> collectMetadataUnitUpDtos
    ) {
        if (Objects.isNull(collectMetadataUnitUpDtos)) {
            return null;
        }
        return collectMetadataUnitUpDtos
            .stream()
            .map(ProjectConverter::toVitamMetadataUnitUp)
            .collect(Collectors.toList());
    }

    public static MetadataUnitUp toVitamMetadataUnitUp(CollectMetadataUnitUpDto collectMetadataUnitUpDto) {
        MetadataUnitUp metadataUnitUp = new MetadataUnitUp();
        metadataUnitUp.setMetadataKey(collectMetadataUnitUpDto.getMetadataKey());
        metadataUnitUp.setMetadataValue(collectMetadataUnitUpDto.getMetadataValue());
        metadataUnitUp.setUnitUp(collectMetadataUnitUpDto.getUnitUp());
        return metadataUnitUp;
    }
}
