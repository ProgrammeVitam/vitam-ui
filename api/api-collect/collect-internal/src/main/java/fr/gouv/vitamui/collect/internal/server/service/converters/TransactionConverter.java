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

import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class TransactionConverter {


    public static List<CollectTransactionDto> toVitamuiDtos(List<TransactionDto> transactionDtos) {
        return transactionDtos.stream().map(TransactionConverter::toVitamUiDto).collect(Collectors.toList());
    }

    public static CollectTransactionDto toVitamUiDto(TransactionDto transactionDto) {
        CollectTransactionDto collectTransactionDto = new CollectTransactionDto();
        collectTransactionDto.setArchivalAgencyIdentifier(transactionDto.getArchivalAgencyIdentifier());
        collectTransactionDto.setArchivalAgreement(transactionDto.getArchivalAgreement());
        collectTransactionDto.setComment(transactionDto.getComment());
        collectTransactionDto.setArchiveProfile(transactionDto.getArchivalProfile());
        collectTransactionDto.setId(transactionDto.getId());
        collectTransactionDto.setStatus(transactionDto.getStatus());
        collectTransactionDto.setMessageIdentifier(transactionDto.getMessageIdentifier());
        collectTransactionDto.setOriginatingAgencyIdentifier(transactionDto.getOriginatingAgencyIdentifier());
        collectTransactionDto.setSubmissionAgencyIdentifier(transactionDto.getSubmissionAgencyIdentifier());
        collectTransactionDto.setTransferringAgencyIdentifier(transactionDto.getTransferringAgencyIdentifier());
        collectTransactionDto.setAcquisitionInformation(transactionDto.getAcquisitionInformation());
        collectTransactionDto.setLegalStatus(transactionDto.getLegalStatus());
        collectTransactionDto.setCreationDate(transactionDto.getCreationDate());
        collectTransactionDto.setLastUpdate(transactionDto.getLastUpdate());
        collectTransactionDto.setProjectId(transactionDto.getProjectId());
        collectTransactionDto.setName(transactionDto.getName());
        return collectTransactionDto;
    }

    public static TransactionDto toVitamDto(CollectTransactionDto collectTransactionDto) {
        TransactionDto externalDto = new TransactionDto();
        externalDto.setArchivalAgencyIdentifier(collectTransactionDto.getArchivalAgencyIdentifier());
        externalDto.setArchivalAgreement(collectTransactionDto.getArchivalAgreement());
        externalDto.setComment(collectTransactionDto.getComment());
        externalDto.setArchivalProfile(collectTransactionDto.getArchiveProfile());
        externalDto.setId(collectTransactionDto.getId());
        externalDto.setMessageIdentifier(collectTransactionDto.getMessageIdentifier());
        externalDto.setOriginatingAgencyIdentifier(collectTransactionDto.getOriginatingAgencyIdentifier());
        externalDto.setSubmissionAgencyIdentifier(collectTransactionDto.getSubmissionAgencyIdentifier());
        externalDto.setTransferringAgencyIdentifier(collectTransactionDto.getTransferringAgencyIdentifier());
        externalDto.setAcquisitionInformation(collectTransactionDto.getAcquisitionInformation());
        externalDto.setLegalStatus(collectTransactionDto.getLegalStatus());
        externalDto.setCreationDate(collectTransactionDto.getCreationDate());
        externalDto.setLastUpdate(collectTransactionDto.getLastUpdate());
        externalDto.setName(collectTransactionDto.getName());
        return externalDto;
    }

    public static TransactionDto updateContextFromProject(ProjectDto projectDto, TransactionDto transactionDto) {
        transactionDto.setArchivalAgencyIdentifier(projectDto.getArchivalAgencyIdentifier());
        transactionDto.setArchivalAgreement(projectDto.getArchivalAgreement());
        transactionDto.setComment(projectDto.getComment());
        transactionDto.setArchivalProfile(projectDto.getArchivalProfile());
        transactionDto.setMessageIdentifier(projectDto.getMessageIdentifier());
        transactionDto.setOriginatingAgencyIdentifier(projectDto.getOriginatingAgencyIdentifier());
        transactionDto.setSubmissionAgencyIdentifier(projectDto.getSubmissionAgencyIdentifier());
        transactionDto.setTransferringAgencyIdentifier(projectDto.getTransferringAgencyIdentifier());
        transactionDto.setAcquisitionInformation(projectDto.getAcquisitionInformation());
        transactionDto.setLegalStatus(projectDto.getLegalStatus());
        transactionDto.setLastUpdate(projectDto.getLastUpdate());
        return transactionDto;
    }

}
