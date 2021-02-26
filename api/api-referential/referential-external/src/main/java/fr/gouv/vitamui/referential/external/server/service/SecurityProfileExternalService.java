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
package fr.gouv.vitamui.referential.external.server.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.internal.client.SecurityProfileInternalRestClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class SecurityProfileExternalService extends AbstractResourceClientService<SecurityProfileDto, SecurityProfileDto> {

    private SecurityProfileInternalRestClient securityProfileInternalRestClient;

    @Autowired
    public SecurityProfileExternalService(ExternalSecurityService externalSecurityService, SecurityProfileInternalRestClient securityProfileInternalRestClient) {
        super(externalSecurityService);
        this.securityProfileInternalRestClient = securityProfileInternalRestClient;
    }

    public List<SecurityProfileDto> getAll(final Optional<String> criteria) {
        return securityProfileInternalRestClient.getAll(getInternalHttpContext(),criteria);
    }

    @Override protected BasePaginatingAndSortingRestClient<SecurityProfileDto, InternalHttpContext> getClient() {
        return securityProfileInternalRestClient;
    }

    public PaginatedValuesDto<SecurityProfileDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction) {

        ParameterChecker.checkPagination(size, page);
        return getClient().getAllPaginated(getInternalHttpContext(), page, size, criteria, orderBy, direction);
    }

    public SecurityProfileDto getOne(String id) {
        return getClient().getOne(getInternalHttpContext(), id);
    }

    @Override
    public SecurityProfileDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    public SecurityProfileDto create(final SecurityProfileDto accessContractDto) {
        return securityProfileInternalRestClient.create(getInternalHttpContext(), accessContractDto);
    }

    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("name", "identifier");
    }

    @Override
    public JsonNode findHistoryById(final String id) {
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public boolean check(SecurityProfileDto accessContractDto) {
        return securityProfileInternalRestClient.check(getInternalHttpContext(), accessContractDto);
    }

    public void delete(final String id) {
        securityProfileInternalRestClient.delete(getInternalHttpContext(), id);
    }

}
