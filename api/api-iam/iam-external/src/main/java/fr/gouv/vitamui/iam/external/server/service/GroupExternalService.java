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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.iam.internal.client.GroupInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the profile groups.
 *
 *
 */
@Getter
@Setter
@Service
public class GroupExternalService extends AbstractResourceClientService<GroupDto, GroupDto> {

    private static final String IDENTIFIER_KEY = "identifier";
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";
    private static final String ENABLED_KEY = "enabled";
    private static final String DESCRIPTION_KEY = "description";

    private final GroupInternalRestClient groupInternalRestClient;

    @Autowired
    public GroupExternalService(final GroupInternalRestClient groupInternalRestClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.groupInternalRestClient = groupInternalRestClient;
    }

    @Override
    public GroupDto create(final GroupDto dto) {
        checkCustomerId(dto.getCustomerId(), "Unable to create group");
        return super.create(dto);
    }

    @Override
    public GroupDto patch(final Map<String, Object> partialDto) {
        final String customerId = (String) partialDto.get("customerId");
        final String level = CastUtils.toString(partialDto.get(LEVEL_KEY));
        if (StringUtils.isNotEmpty(customerId)) {
            checkCustomerId(customerId, "Unable to patch group");
        }
        if (StringUtils.isNotEmpty(level)) {
            checkLevel(level, "Unable to patch profile");
        }
        partialDto.put("customerId", externalSecurityService.getCustomerId());
        return super.patch(partialDto);
    }

    @Override
    public GroupDto getOne(final String id, final Optional<String> embedded) {
        return super.getOne(id, embedded);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    public List<GroupDto> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return super.getAll(criteria, embedded);
    }

    @Override
    public PaginatedValuesDto<GroupDto> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction,
            final Optional<String> embedded) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction, embedded);
    }

    @Override
    public Collection<String> getAllowedKeys() {
        return Arrays.asList(ID_KEY, IDENTIFIER_KEY, NAME_KEY, ENABLED_KEY, DESCRIPTION_KEY, LEVEL_KEY, CUSTOMER_ID_KEY);
    }

    @Override
    protected void addRestriction(final String key, final QueryDto query) {
        switch (key) {
            case LEVEL_KEY :
                addLevelRestriction(query);
                break;
            default :
                throw new NotImplementedException("Restriction not defined for key: " + key);
        }
    }

    /**
     * If the user is not an admin, he can only see groups with a sub LEVEL and his group
     * Example : Users { id: 10, level: ROOT} can see only groups at level : ROOT..* and his group
     * @param query
     */
    private void addLevelRestriction(final QueryDto query) {
        final QueryDto levelQuery = new QueryDto();
        levelQuery.setQueryOperator(QueryOperator.OR);
        levelQuery.addCriterion("level", externalSecurityService.getLevel() + ".", CriterionOperator.STARTWITH);
        levelQuery.addCriterion("id", externalSecurityService.getUser().getProfileGroup().getId(),
                CriterionOperator.EQUALS);
        query.addQuery(levelQuery);
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        final Collection<String> restrictedKeys = new ArrayList<>(Arrays.asList(CUSTOMER_ID_KEY, LEVEL_KEY));
        if (externalSecurityService.userIsRootLevel()) {
            restrictedKeys.remove(LEVEL_KEY);
        }
        return restrictedKeys;
    }

    @Override
    protected GroupInternalRestClient getClient() {
        return groupInternalRestClient;
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public void checkLogbookRight(final String id) {
        final boolean hasRoleGetGroups = externalSecurityService.hasRole(ServicesData.ROLE_GET_GROUPS);
        if (!hasRoleGetGroups) {
            if (!StringUtils.equals(externalSecurityService.getUser().getGroupId(), id)) {
                throw new ForbiddenException(String.format("Unable to access group with id: %s", id));
            }
        }
        final GroupDto groupDto = super.getOne(id);
        if (groupDto == null) {
            throw new ForbiddenException(String.format("Unable to access group with id: %s", id));
        }
    }

    public List<String> getLevels(Optional<String> criteria) {
        return getClient().getLevels(getInternalHttpContext(), checkAuthorization(criteria));
    }
}
