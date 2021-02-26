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
package fr.gouv.vitamui.referential.common.utils;

import java.util.HashSet;
import java.util.Set;

import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.ContextStatus;
import fr.gouv.vitamui.referential.common.dto.*;

public class ReferentialDtoBuilder {

    public static ContextDto buildContextDto(final String id) {
        final ContextDto contextDto = new ContextDto();
        contextDto.setId(id);
        contextDto.setIdentifier("identifier");
        contextDto.setName("name");
        contextDto.setStatus(ContextStatus.ACTIVE.toString());
        contextDto.setEnableControl(true);
        contextDto.setSecurityProfile("securityProfile");

        return contextDto;
    }

    public static RuleDto buildRuleDto(final String id, final String ruleId,
                                       final String ruleType, final String ruleValue,
                                       final String ruleDescription, final String ruleDuration,
                                       final String ruleMeasurement) {
        final RuleDto ruleDto = new RuleDto();
        ruleDto.setId(id);
        ruleDto.setRuleId(ruleId);
        ruleDto.setRuleType(ruleType);
        ruleDto.setRuleValue(ruleValue);
        ruleDto.setRuleDescription(ruleDescription);
        ruleDto.setRuleDuration(ruleDuration);
        ruleDto.setRuleMeasurement(ruleMeasurement);

        return ruleDto;
    }

    public static AccessContractDto buildAccessContractDto(final String id) {
        final AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setId(id);
        accessContractDto.setIdentifier("identifier");
        accessContractDto.setName("name");
        accessContractDto.setStatus(ActivationStatus.ACTIVE.toString());

        return accessContractDto;
    }

    public static IngestContractDto buildIngestContractDto(final String id) {
        final IngestContractDto ingestContractDto = new IngestContractDto();
        ingestContractDto.setId(id);
        ingestContractDto.setIdentifier("identifier");
        ingestContractDto.setName("name");
        ingestContractDto.setStatus(ActivationStatus.ACTIVE);

        return ingestContractDto;
    }

    public static SecurityProfileDto buildSecurityProfileDto(final String id) {
        final SecurityProfileDto securityProfileDto = new SecurityProfileDto();
        securityProfileDto.setId(id);
        securityProfileDto.setIdentifier("identifier");
        securityProfileDto.setName("name");

        return securityProfileDto;
    }

    private static Set<PermissionDto> buildPermissions() {
        Set<String> contracts = new HashSet<String>();
        contracts.add("contract");

        PermissionDto permission1 = new PermissionDto();
        permission1.setTenant("tenant_1");
        permission1.setAccessContracts(contracts);
        permission1.setIngestContracts(contracts);

        PermissionDto permission2 = new PermissionDto();
        permission2.setTenant("tenant_2");
        permission2.setAccessContracts(contracts);
        permission2.setIngestContracts(contracts);

        Set<PermissionDto> permissions = new HashSet<PermissionDto>();
        permissions.add(permission1);
        permissions.add(permission2);
        return permissions;
    }

}
