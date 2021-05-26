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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.internal.client.ExternalParamProfileInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.ProfileInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The service to read, create, update and delete the external parameters.
 *
 *
 */
@Getter
@Setter
@Service
public class ExternalParamProfileExternalService extends AbstractResourceClientService<ExternalParamProfileDto, ExternalParamProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ExternalParamProfileExternalService.class);

	private final ExternalParamProfileInternalRestClient externalParamProfileInternalRestClient;
    private final ProfileInternalRestClient profileInternalRestClient;

    @Autowired
    public ExternalParamProfileExternalService(final ExternalParamProfileInternalRestClient externalParamProfileInternalRestClient,
                                                final ProfileInternalRestClient profileInternalRestClient,
    		                                    final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.externalParamProfileInternalRestClient = externalParamProfileInternalRestClient;
        this.profileInternalRestClient = profileInternalRestClient;
    }

    @Override
    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(final Integer page, final Integer size,
        final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction,
        final Optional<String> embedded) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction, embedded);
    }

    @Override
    public ExternalParamProfileDto getOne(String id) {
        return super.getOne(id);
    }

    @Override
    public ExternalParamProfileDto create(final ExternalParamProfileDto externalParamProfileDto) {
        return super.create(externalParamProfileDto);
    }

    @Override
    public ExternalParamProfileDto update(final ExternalParamProfileDto externalParamProfileDto) {
        return super.update(externalParamProfileDto);
    }

    @Override
    public ExternalParamProfileDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    @Override
    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return super.findHistoryById(id);
    }

    private void checkLogbookRight(final String id) {
        final boolean hasRoleGetUsers = externalSecurityService.hasRole(ServicesData.ROLE_GET_PROFILES);
        if (!hasRoleGetUsers && !externalSecurityService.getUser().getProfileGroup().getProfileIds().contains(id)) {
            LOGGER.info("The profile is not in the group of the authenticated user");
             throw new ForbiddenException(String.format("Unable to access profile with id: %s", id));
        }
        final ProfileDto profileDto = profileInternalRestClient.getOne(getInternalHttpContext(), id);
        if (profileDto == null) {
            throw new ForbiddenException(String.format("Unable to access profile with id: %s", id));
        }
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    protected ExternalParamProfileInternalRestClient getClient() {
        return externalParamProfileInternalRestClient;
    }

    @Override
    public Collection<String> getAllowedKeys() {
        return List.of("applicationName", "name", "tenantIdentifier");
    }
}
