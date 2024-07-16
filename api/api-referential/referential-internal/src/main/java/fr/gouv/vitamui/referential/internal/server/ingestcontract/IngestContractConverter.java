/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.internal.server.ingestcontract;

import fr.gouv.vitam.common.model.administration.IngestContractCheckState;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.SignaturePolicy;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.dto.SignaturePolicyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IngestContractConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestContractConverter.class);

    public IngestContractModel convertDtoToVitam(final IngestContractDto dto) {
        final IngestContractModel ingestContract = VitamUIUtils.copyProperties(dto, new IngestContractModel());
        if (dto.getCheckParentId() != null) {
            ingestContract.setCheckParentId(new HashSet<>(dto.getCheckParentId()));
        }
        if (dto.getCheckParentLink() != null) {
            ingestContract.setCheckParentLink(IngestContractCheckState.valueOf(dto.getCheckParentLink()));
        }

        ingestContract.setCreationdate(dto.getCreationDate());
        ingestContract.setLastupdate(dto.getLastUpdate());
        ingestContract.setActivationdate(dto.getActivationDate());
        ingestContract.setDeactivationdate(dto.getDeactivationDate());
        ingestContract.setComputeInheritedRulesAtIngest(dto.isComputeInheritedRulesAtIngest());
        ingestContract.setSignaturePolicy(convertDtoToVitam(dto.getSignaturePolicy()));

        return ingestContract;
    }

    public IngestContractDto convertVitamToDto(final IngestContractModel ingestContract) {
        final IngestContractDto dto = VitamUIUtils.copyProperties(ingestContract, new IngestContractDto());
        if (ingestContract.getCheckParentLink() != null) {
            dto.setCheckParentLink(ingestContract.getCheckParentLink().name());
        }

        // copyProperties() doesn't handle Boolean properties
        dto.setMasterMandatory(ingestContract.isMasterMandatory());
        dto.setFormatUnidentifiedAuthorized(ingestContract.isFormatUnidentifiedAuthorized());
        dto.setEveryFormatType(ingestContract.isEveryFormatType());
        dto.setEveryDataObjectVersion(ingestContract.isEveryDataObjectVersion());
        dto.setComputeInheritedRulesAtIngest(ingestContract.isComputeInheritedRulesAtIngest());
        dto.setCreationDate(ingestContract.getCreationdate());
        dto.setLastUpdate(ingestContract.getLastupdate());
        dto.setActivationDate(ingestContract.getActivationdate());
        dto.setDeactivationDate(ingestContract.getDeactivationdate());
        dto.setSignaturePolicy(convertVitamToDto(ingestContract.getSignaturePolicy()));

        return dto;
    }

    public SignaturePolicyDto convertVitamToDto(final SignaturePolicy vitam) {
        if (Objects.isNull(vitam)) {
            return null;
        }
        return new SignaturePolicyDto()
            .setSignedDocument(vitam.getSignedDocument())
            .setDeclaredSignature(vitam.isDeclaredSignature())
            .setDeclaredTimestamp(vitam.isDeclaredTimestamp())
            .setDeclaredAdditionalProof(vitam.isDeclaredAdditionalProof());
    }

    public SignaturePolicy convertDtoToVitam(final SignaturePolicyDto dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        SignaturePolicy signaturePolicy = new SignaturePolicy();
        signaturePolicy.setSignedDocument(dto.getSignedDocument());
        signaturePolicy.setDeclaredSignature(dto.getDeclaredSignature());
        signaturePolicy.setDeclaredTimestamp(dto.getDeclaredTimestamp());
        signaturePolicy.setDeclaredAdditionalProof(dto.getDeclaredAdditionalProof());
        return signaturePolicy;
    }

    public List<IngestContractModel> convertDtosToVitams(final List<IngestContractDto> dtos) {
        return dtos.stream().map(this::convertDtoToVitam).collect(Collectors.toList());
    }

    public List<IngestContractDto> convertVitamsToDtos(final List<IngestContractModel> ingestContracts) {
        return ingestContracts.stream().map(this::convertVitamToDto).collect(Collectors.toList());
    }
}
