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

package fr.gouv.vitamui.collect.internal.server.service.project;

import fr.gouv.vitam.collect.external.dto.ProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectListProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CollectProjectConverter {
    public static CollectProjectDto toVitamuiDto(ProjectDto projectDto) {
        return CollectProjectDto.builder()
            .archivalAgencyIdentifier(projectDto.getArchivalAgencyIdentifier())
            .archivalAgreement(projectDto.getArchivalAgreement())
            .archivalProfile(projectDto.getArchivalProfile())
            .comment(projectDto.getComment())
            .id(projectDto.getId())
            .messageIdentifier(projectDto.getMessageIdentifier())
            .unitUp(projectDto.getUnitUp())
            .transactionId(projectDto.getTransactionId())
            .originatingAgencyIdentifier(projectDto.getOriginatingAgencyIdentifier())
            .submissionAgencyIdentifier(projectDto.getSubmissionAgencyIdentifier())
            .transferringAgencyIdentifier(projectDto.getTransferingAgencyIdentifier())
            .build();
    }

    public static List<CollectProjectDto> toVitamuiDtos(List<CollectListProjectDto> collectListProjectDtos) {

        return collectListProjectDtos.stream().map(collectListProjectDto -> {

            CollectListProjectDto.Context context = collectListProjectDto.getContext();

            return CollectProjectDto.builder()
                .archivalAgencyIdentifier(context.getArchivalAgencyIdentifier())
                .archivalAgreement(context.getArchivalAgreement())
                .archivalProfile(context.getArchivalProfile())
                .comment(context.getComment())
                .id(collectListProjectDto.getId())
                .messageIdentifier(context.getMessageIdentifier())
                .originatingAgencyIdentifier(context.getOriginatingAgencyIdentifier())
                .submissionAgencyIdentifier(context.getSubmissionAgencyIdentifier())
                .transferringAgencyIdentifier(context.getTransferingAgencyIdentifier())
                .build();
        }).collect(Collectors.toList());
    }

    public static ProjectDto toVitamDto(CollectProjectDto collectProjectDto) {
        ProjectDto externalDto = new ProjectDto();
        externalDto.setArchivalAgencyIdentifier(collectProjectDto.getArchivalAgencyIdentifier());
        externalDto.setArchivalAgreement(collectProjectDto.getArchivalAgreement());
        externalDto.setComment(collectProjectDto.getComment());
        externalDto.setArchivalProfile(collectProjectDto.getArchivalProfile());
        externalDto.setId(collectProjectDto.getId());
        externalDto.setMessageIdentifier(collectProjectDto.getMessageIdentifier());
        externalDto.setOriginatingAgencyIdentifier(collectProjectDto.getOriginatingAgencyIdentifier());
        externalDto.setSubmissionAgencyIdentifier(collectProjectDto.getSubmissionAgencyIdentifier());
        externalDto.setTransferingAgencyIdentifier(collectProjectDto.getTransferringAgencyIdentifier());
        externalDto.setTransactionId(collectProjectDto.getTransactionId());
        externalDto.setUnitUp(collectProjectDto.getUnitUp());
        return externalDto;
    }
}
