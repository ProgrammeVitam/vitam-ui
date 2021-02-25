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
package fr.gouv.vitamui.iam.external.server.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.iam.internal.client.OwnerInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the tenants.
 *
 *
 */
@Getter
@Setter
@Service
public class OwnerExternalService extends AbstractResourceClientService<OwnerDto, OwnerDto> {

    private final OwnerInternalRestClient ownerInternalRestClient;

    @Autowired
    public OwnerExternalService(final OwnerInternalRestClient ownerInternalRestClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.ownerInternalRestClient = ownerInternalRestClient;
    }

    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public void checkLogbookRight(final String id) {
        final boolean hasRoleGetOwner = externalSecurityService.hasRole(ServicesData.ROLE_GET_OWNERS);
        if (!hasRoleGetOwner) {
            // TODO
        }
        final OwnerDto ownerDto = super.getOne(id);
        if (ownerDto == null) {
            throw new ForbiddenException(String.format("Unable to access owner with id: %s", id));
        }
    }

    @Override
    public OwnerDto create(final OwnerDto dto) {
        return super.create(dto);
    }

    @Override
    public OwnerDto update(final OwnerDto dto) {
        return super.update(dto);
    }

    @Override
    public OwnerDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    @Override
    public OwnerDto getOne(final String id) {
        return super.getOne(id);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    protected OwnerInternalRestClient getClient() {
        return ownerInternalRestClient;
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("id", "name", "code", "companyName", CUSTOMER_ID_KEY);
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return Collections.emptyList();
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

}
