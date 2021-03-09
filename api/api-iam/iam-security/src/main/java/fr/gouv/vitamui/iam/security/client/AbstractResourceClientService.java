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
package fr.gouv.vitamui.iam.security.client;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for ExternalVitamUICrudService
 *
 *
 * @param <E> External DTO type
 * @param <I> Internal DTO Type
 */
@Getter
@Setter
public abstract class AbstractResourceClientService<E extends IdDto, I extends IdDto> extends AbstractInternalClientService {

    protected static final String EXTERNAL_PARAM_ID_KEY = "externalParamId";

    protected static final String TENANT_IDENTIFIER_KEY = "tenantIdentifier";

    protected static final String CUSTOMER_ID_KEY = "customerId";

    protected static final String LEVEL_KEY = "level";

    protected static final String CRITERIA_VERSION_V1 = "v1";

    protected static final String CRITERIA_VERSION_V2 = "v2";


    public AbstractResourceClientService(final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
    }

    protected E create(final E dto) {
        ApiUtils.checkValidity(dto);
        return converterToExternalDto(getClient().create(getInternalHttpContext(), converterToInternalDto(dto)));
    }

    protected E patch(final Map<String, Object> partialDto) {
        return converterToExternalDto(getClient().patch(getInternalHttpContext(), partialDto));
    }

    protected E patchWithDto(final I partialDto) {
        return converterToExternalDto(getClient().patchWithDto(getInternalHttpContext(), partialDto));
    }

    protected E getOne(final String id) {
        final Optional<String> criteria = addAccessRestriction(new QueryDto()).toOptionalJson();
        return converterToExternalDto(getClient().getOne(getInternalHttpContext(), id, criteria));
    }

    protected E getOne(final String id, final Optional<String> embedded) {
        final Optional<String> criteria = addAccessRestriction(new QueryDto()).toOptionalJson();
        return converterToExternalDto(getClient().getOne(getInternalHttpContext(), id, criteria, embedded));
    }

    protected PaginatedValuesDto<E> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        ParameterChecker.checkPagination(size, page);
        final PaginatedValuesDto<I> result = getClient().getAllPaginated(getInternalHttpContext(), page, size,
                checkAuthorization(criteria), orderBy, direction);
        return new PaginatedValuesDto<>(result.getValues().stream().map(element -> converterToExternalDto(element))
                .collect(Collectors.toList()), result.getPageNum(), result.getPageSize(), result.isHasMore());
    }

    protected PaginatedValuesDto<E> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction,
            final Optional<String> embedded) {
        ParameterChecker.checkPagination(size, page);
        final PaginatedValuesDto<I> result = getClient().getAllPaginated(getInternalHttpContext(), page, size,
                checkAuthorization(criteria), orderBy, direction, embedded);
        return new PaginatedValuesDto<>(result.getValues().stream().map(element -> converterToExternalDto(element))
                .collect(Collectors.toList()), result.getPageNum(), result.getPageSize(), result.isHasMore());
    }

    /**
     * Check the criteria. <br>
     *
     * Convert Json to CriteriaWrapper <br>
     * Check if the criteria keys are allowed
     * @param criteria
     * @return
     * @throws InvalidFormatException
     */
    protected Optional<String> checkAuthorization(final Optional<String> criteria) throws InvalidFormatException {
        return addAccessRestriction(QueryDto.fromJson(criteria)).toOptionalJson();
    }

    /**
     * Check the criteria. <br>
     *
     * Convert Json to CriteriaWrapper <br>
     * Check if the criteria keys are allowed
     * @param criteria
     * @return
     * @throws InvalidFormatException
     */
    protected Optional<QueryDto> checkCriteriaAuthorization(final Optional<QueryDto> criteria)
            throws InvalidFormatException {
        return Optional.of(addAccessRestriction(criteria.orElse(new QueryDto())));
    }

    protected QueryDto addAccessRestriction(final QueryDto query) {
        if (CRITERIA_VERSION_V1.equals(getVersionApiCrtieria())) {
            return addAccessRestrictionV1(query);
        } else if (CRITERIA_VERSION_V2.equals(getVersionApiCrtieria())) {
            return addAccessRestrictionV2(query);
        } else {
            throw new NotImplementedException("Unknow version");
        }
    }

    /**
     * Create an andQuery and add security filter
     * @param query
     * @return
     */
    protected QueryDto addAccessRestrictionV2(final QueryDto query) {
        checkContainsAuthorizedKeys(query);
        final QueryDto securedQuery = QueryDto.andQuery();
        query.keyFilter(getAllowedKeys());
        getRestrictedKeys().forEach(key -> addAccessRestrictionByKey(key, securedQuery));
        securedQuery.addQuery(query);
        return securedQuery;
    }

    /**
     * Check if the query and subqueries only contains criteria on allowed keys.
     * @param query
     * @throws ForbiddenException If a key is not allowed
     */
    private void checkContainsAuthorizedKeys(final QueryDto query) {
        query.keyFilter(getAllowedKeys());
        if (query.getSubQueries() != null) {
            query.getSubQueries().forEach((q) -> {
                checkContainsAuthorizedKeys(q);
            });
        }

    }

    protected QueryDto addAccessRestrictionV1(final QueryDto criteria) {
        criteria.keyFilter(getAllowedKeys());
        getRestrictedKeys().forEach(key -> addAccessRestrictionByKey(key, criteria));
        if (!(criteria.getQueryOperator() == null || QueryOperator.AND.equals(criteria.getQueryOperator()))) {
            throw new UnsupportedOperationException("Unsupported operator " + criteria.getQueryOperator()
            + ". This API only supports the queryOperator \"AND\" on the root query.");
        }
        return criteria;
    }

    /**
     * Override this method for use the desired version
     * @return
     */
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V1;
    }

    protected void addAccessRestrictionByKey(final String key, final QueryDto criteria) {
        switch (key) {
            case CUSTOMER_ID_KEY :
                addCustomerRestriction(criteria);
                break;
            case TENANT_IDENTIFIER_KEY :
                addTenantIdentifierRestriction(criteria);
                break;
            default :
                addRestriction(key, criteria);
                break;
        }
    }

    /**
     * Method allowing to defined restrictions for criterion's field.
     * This method must be implemented by any service using restrictions.
     * @param key Key of the restriction.
     * @param criteria Criteria linked to the search.
     */
    protected void addRestriction(final String key, final QueryDto criteria) {
        throw new NotImplementedException("Restriction not defined for key: " + key);
    }

    protected void addTenantIdentifierRestriction(final QueryDto criteria) {
        final Optional<Criterion> optCriteria = criteria.find(TENANT_IDENTIFIER_KEY);
        if (optCriteria.isPresent()) {
            checkTenantIdentifierCriteria(optCriteria.get());
        } else {
            criteria.addCriterion(getTenantIdentifierRestriction());
        }

    }

    protected void checkTenantIdentifierCriteria(final Criterion tenantIdentifierCriteria) {
        if (!CastUtils.toInteger(tenantIdentifierCriteria.getValue())
                .equals(externalSecurityService.getTenantIdentifier())
                || !tenantIdentifierCriteria.getOperator().equals(CriterionOperator.EQUALS)) {
            throw new ForbiddenException(
                    "tenantIdentifier's criteria is not equal to the tenantIdentifier from context");
        }
    }

    protected void addCustomerRestriction(final QueryDto criteria) {
        final Optional<Criterion> customerCriteria = criteria.find(CUSTOMER_ID_KEY);
        if (customerCriteria.isPresent()) {
            checkCustomerCriteria(customerCriteria.get());
        } else {
            criteria.addCriterion(getCustomerRestriction());
        }
    }

    protected void checkCustomerCriteria(final Criterion customerCriteria) {
        if (!StringUtils.equals(CastUtils.toString(customerCriteria.getValue()),
                externalSecurityService.getCustomerId())
                || !customerCriteria.getOperator().equals(CriterionOperator.EQUALS)) {
            throw new ForbiddenException("customerId's criteria is not equal to the customerId from context");
        }
    }

    /**
     * Override for add restriction, like customerId, tenantIdentifier etc.
     *
     */
    protected Collection<String> getRestrictedKeys() {
        return Arrays.asList(CUSTOMER_ID_KEY);
    }

    /**
     * The Collection contains keys allowed
     * By default the collection is null and all keys are authorized
     * Function as a whitelist
     * @return
     */
    protected Collection<String> getAllowedKeys() {
        return Collections.emptyList();
    }

    protected Criterion getCustomerRestriction() {
        return new Criterion(CUSTOMER_ID_KEY, externalSecurityService.getCustomerId(), CriterionOperator.EQUALS);
    }

    protected Criterion getTenantIdentifierRestriction() {
        return new Criterion(TENANT_IDENTIFIER_KEY, externalSecurityService.getTenantIdentifier(),
                CriterionOperator.EQUALS);
    }

    protected E update(final E dto) {
        ApiUtils.checkValidity(dto);
        return converterToExternalDto(getClient().update(getInternalHttpContext(), converterToInternalDto(dto)));
    }

    protected List<E> getAll(final Optional<String> criteria) {
        return getClient().getAll(getInternalHttpContext(), checkAuthorization(criteria)).stream()
                .map(element -> converterToExternalDto(element)).collect(Collectors.toList());
    }

    protected List<E> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return getClient().getAll(getInternalHttpContext(), checkAuthorization(criteria), embedded).stream()
                .map(element -> converterToExternalDto(element)).collect(Collectors.toList());
    }

    protected boolean checkExists(final String criteria) {
        return getClient().checkExist(getInternalHttpContext(),
                checkAuthorization(Optional.ofNullable(criteria)).get());
    }

    protected void delete(final String id) {
        getClient().delete(getInternalHttpContext(), id);
    }

    @Override
    protected abstract BasePaginatingAndSortingRestClient<I, InternalHttpContext> getClient();

    protected void checkCustomerId(final String customerId, final String message) {
        Assert.isTrue(StringUtils.equals(customerId, externalSecurityService.getCustomerId()),
                message + ": customerId " + customerId + " is not allowed");
    }

    protected void checkTenantIdentifier(final Integer tenantIdentifier, final String message) {
        Assert.isTrue(externalSecurityService.getTenantIdentifier().equals(tenantIdentifier),
                message + ": tenantIdentifier " + tenantIdentifier + " is not allowed");
    }

    protected void checkLevel(final String level, final String message) {
        Assert.isTrue(externalSecurityService.isLevelAllowed(level), message);
    }

    protected JsonNode findHistoryById(final String id) {
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    @SuppressWarnings("unchecked")
    protected E converterToExternalDto(final I internalDto) {
        return (E) internalDto;
    }

    @SuppressWarnings("unchecked")
    protected I converterToInternalDto(final E externalDto) {
        return (I) externalDto;
    }
}
