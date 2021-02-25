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

import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the users.
 *
 *
 */
@Getter
@Setter
@Service
public class UserExternalService extends AbstractResourceClientService<UserDto, UserDto> {

    private static final String TYPE_KEY = "type";

    private static final String LEVEL_KEY = "level";

    private final UserInternalRestClient userInternalRestClient;

    @Autowired
    public UserExternalService(final UserInternalRestClient userInternalRestClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.userInternalRestClient = userInternalRestClient;
    }

    @Override
    public UserDto create(final UserDto dto) {
        checkCustomerId(dto.getCustomerId(), "Unable to create user");
        return super.create(dto);
    }

    @Override
    public UserDto update(final UserDto dto) {
        checkCustomerId(dto.getCustomerId(), "Unable to update user");
        return super.update(dto);
    }

    @Override
    public UserDto patch(final Map<String, Object> partialDto) {
        final String customerId = (String) partialDto.get("customerId");

        if (StringUtils.isNotEmpty(customerId)) {
            checkCustomerId(customerId, "Unable to patch user");
        }

        partialDto.put("customerId", externalSecurityService.getCustomerId());

        return super.patch(partialDto);
    }

    public UserDto patchMe(final Map<String, Object> partialDto) {
        final AuthUserDto user = externalSecurityService.getUser();
        partialDto.put("id", user.getId());
        return patch(partialDto);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    public UserDto getOne(final String id) {
        return super.getOne(id);
    }

    @Override
    public PaginatedValuesDto<UserDto> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Override
    protected void addRestriction(final String key, final QueryDto query) {
        switch (key) {
            case TYPE_KEY :
                addTypeRestriction(query);
                break;
            case LEVEL_KEY :
                addLevelRestriction(query);
                break;
            default :
                throw new NotImplementedException("Restriction not defined for key: " + key);
        }
    }

    /**
     * If the user is not an admin, he can see only users with a sub LEVEL and himself
     * Example : Users { id: 10, level: ROOT} can see only users with a LEVEL : ROOT..* and himself
     * @param query
     */
    private void addLevelRestriction(final QueryDto query) {
        final QueryDto levelQuery = new QueryDto();
        levelQuery.setQueryOperator(QueryOperator.OR);
        levelQuery.addCriterion("level", externalSecurityService.getLevel() + ".", CriterionOperator.STARTWITH);
        levelQuery.addCriterion("id", externalSecurityService.getUser().getId(), CriterionOperator.EQUALS);
        query.addQuery(levelQuery);
    }

    private void addTypeRestriction(final QueryDto criteria) {
        final Optional<Criterion> typeCriterion = criteria.find(TYPE_KEY);
        if (typeCriterion.isPresent()) {
            final Criterion criterion = typeCriterion.get();
            final UserTypeEnum userType = EnumUtils.stringToEnum(UserTypeEnum.class, criterion.getValue().toString());
            if (!(criterion.getOperator().equals(CriterionOperator.EQUALS)
                    && userType.equals(UserTypeEnum.NOMINATIVE))) {
                throw new ForbiddenException(String.format("User's type %s is not allowed", userType));
            }
        } else {
            criteria.addCriterion(new Criterion(TYPE_KEY, UserTypeEnum.NOMINATIVE, CriterionOperator.EQUALS));
        }
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("id", "lastname", "firstname", "identifier", "groupId", "language", "email", "otp",
                "subrogeable", "phone", "mobile", "lastConnection", "status", "level", TYPE_KEY, CUSTOMER_ID_KEY, "siteCode");
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        final Collection<String> restrictedKeys = new ArrayList<>(Arrays.asList(CUSTOMER_ID_KEY, TYPE_KEY, LEVEL_KEY));
        if (externalSecurityService.hasRole(ServicesData.ROLE_GENERIC_USERS)) {
            restrictedKeys.remove(TYPE_KEY);
        }
        if (externalSecurityService.userIsRootLevel()) {
            restrictedKeys.remove(LEVEL_KEY);
        }

        return restrictedKeys;
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

    @Override
    protected UserInternalRestClient getClient() {
        return userInternalRestClient;
    }

    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public void checkLogbookRight(final String id) {
        final boolean hasRoleGetUsers = externalSecurityService.hasRole(ServicesData.ROLE_GET_USERS);
        if (!hasRoleGetUsers) {
            if (!StringUtils.equals(externalSecurityService.getUser().getId(), id)) {
                throw new ForbiddenException(String.format("Unable to access user with id: %s", id));
            }
        }
        final UserDto usersDto = super.getOne(id);
        if (usersDto == null) {
            throw new ForbiddenException(String.format("Unable to access user with id: %s", id));
        }
    }


    public List<String> getLevels(final Optional<String> criteria) {
        return getClient().getLevels(getInternalHttpContext(), checkAuthorization(criteria));
    }

    public UserDto patchAnalytics(final Map<String, Object> partialDto) {
        return getClient().patchAnalytics(getInternalHttpContext(), partialDto);
    }

}
