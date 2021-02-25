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
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.internal.client.SubrogationInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the subrogations.
 *
 *
 */
@Getter
@Setter
@Service
public class SubrogationExternalService extends AbstractResourceClientService<SubrogationDto, SubrogationDto> {

    private final SubrogationInternalRestClient subrogationInternalRestClient;

    @Autowired
    public SubrogationExternalService(final ExternalSecurityService externalSecurityService,
            final SubrogationInternalRestClient subrogationInternalRestClient) {
        super(externalSecurityService);
        this.subrogationInternalRestClient = subrogationInternalRestClient;
    }

    @Override
    public List<SubrogationDto> getAll(final Optional<String> criteria) {
        return super.getAll(criteria);
    }

    @Override
    public SubrogationDto getOne(final String id) {
        return super.getOne(id);
    }

    @Override
    public SubrogationDto create(final SubrogationDto dto) {
        return super.create(dto);
    }

    public void decline(final String id) {
        getClient().decline(getInternalHttpContext(), id);
    }

    @Override
    public void delete(final String id) {
        getClient().delete(getInternalHttpContext(), id);
    }

    public SubrogationDto accept(final String id) {
        return getClient().accept(getInternalHttpContext(), id);
    }

    public SubrogationDto getMySubrogationAsSurrogate() {
        return getClient().getMySubrogationAsSurrogate(getInternalHttpContext());
    }

    public SubrogationDto getMySubrogationAsSuperuser() {
        return getClient().getMySubrogationAsSuperuser(getInternalHttpContext());
    }

    public PaginatedValuesDto<UserDto> getGenericUsers(final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        final QueryDto criteriaFiltered = QueryDto.fromJson(criteria);
        criteriaFiltered.addCriterion("type", UserTypeEnum.GENERIC, CriterionOperator.EQUALS);

        return getClient().getUsers(getInternalHttpContext(), page, size, criteriaFiltered.toOptionalJson(), orderBy,
                direction);
    }

    @Override
    protected SubrogationInternalRestClient getClient() {
        return subrogationInternalRestClient;
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return CollectionUtils.emptyCollection();
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("surrogateCustomerId");
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

    public GroupDto getGroupById(final String id) {
        return getClient().getGroupById(getInternalHttpContext(), id, Optional.of(EmbeddedOptions.ALL.toString()));
    }
}
