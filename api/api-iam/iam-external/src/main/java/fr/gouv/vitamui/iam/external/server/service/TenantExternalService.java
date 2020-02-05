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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.iam.internal.client.TenantInternalRestClient;
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
public class TenantExternalService extends AbstractResourceClientService<TenantDto, TenantDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantExternalService.class);

    private final TenantInternalRestClient tenantInternalRestClient;

    private final String CUSTOMER_INSUFFICIENT_PERMISSION_MESSAGE = "Unable to update the tenant %s: insufficient permissions on customer.";

    private final String TENANT_INSUFFICIENT_PERMISSION_MESSAGE = "Unable to access to the tenant %s: insufficient permissions.";

    private final String ID_KEY = "id";

    @Autowired
    public TenantExternalService(final ExternalSecurityService externalSecurityService, final TenantInternalRestClient tenantInternalRestClient) {
        super(externalSecurityService);
        this.tenantInternalRestClient = tenantInternalRestClient;

    }

    @Override
    public TenantDto create(final TenantDto dto) {
        LOGGER.debug("Create {}", dto);
        if (!canAccessToCustomer(dto.getCustomerId())) {
            throw new ForbiddenException(String.format(CUSTOMER_INSUFFICIENT_PERMISSION_MESSAGE, dto.getId()));
        }
        return super.create(dto);
    }

    @Override
    public TenantDto getOne(final String id) {
        return super.getOne(id);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    public TenantDto update(final TenantDto tenant) {
        ApiUtils.checkValidity(tenant);

        if (!canAccessToTenant(tenant.getIdentifier())) {
            throw new NoRightsException(String.format(TENANT_INSUFFICIENT_PERMISSION_MESSAGE, tenant.getId()));
        }

        if (!canAccessToCustomer(tenant.getCustomerId())) {
            throw new NoRightsException(String.format(CUSTOMER_INSUFFICIENT_PERMISSION_MESSAGE, tenant.getId()));
        }
        return getClient().update(getInternalHttpContext(), tenant);
    }

    @Override
    public TenantDto patch(final Map<String, Object> partialDto) {
        final String id = (String) partialDto.get(ID_KEY);
        final TenantDto tenant = super.getOne(id, Optional.empty());

        if (!canAccessToTenant(tenant.getIdentifier())) {
            throw new NoRightsException(String.format(TENANT_INSUFFICIENT_PERMISSION_MESSAGE, tenant.getId()));
        }

        if (partialDto.containsKey(CUSTOMER_ID_KEY)) {
            final String customerId = CastUtils.toString(partialDto.get(CUSTOMER_ID_KEY));
            if (!canAccessToCustomer(customerId)) {
                throw new NoRightsException(String.format(CUSTOMER_INSUFFICIENT_PERMISSION_MESSAGE, tenant.getId()));
            }
        }

        return super.patch(partialDto);
    }

    @Override
    public List<TenantDto> getAll(final Optional<String> criteria) {
        return super.getAll(criteria);
    }

    /**
     * Method allowing to check if a current user can update the tenant with the following customer.
     * @param customerId Identifier of the customer.
     * @return true if the action is allowed, false otherwise.
     */
    protected boolean canAccessToCustomer(final String customerId) {
        if (!externalSecurityService.hasRole(ServicesData.ROLE_UPDATE_TENANTS_ALL_CUSTOMERS) && !customerId.equals(externalSecurityService.getCustomerId())) {
            return false;
        }
        return true;
    }

    /**
     * Check if a current user can access the tenant.
     * @param tenantIdentifier Identifier of the tenant.
     * @return true if the action is allowed, false otherwise.
     */
    protected boolean canAccessToTenant(final Integer tenantIdentifier) {
        if (!externalSecurityService.hasRole(ServicesData.ROLE_GET_ALL_TENANTS)) {
            final Integer securityTenantIdentifier = externalSecurityService.getTenantIdentifier();
            return Objects.equals(securityTenantIdentifier, tenantIdentifier);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("id", CUSTOMER_ID_KEY, "enabled", "proof", "name", "identifier", "ownerId");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getRestrictedKeys() {
        final List<String> restrictedKeys = new ArrayList<>();
        if (!externalSecurityService.hasRole(ServicesData.ROLE_GET_ALL_TENANTS)) {
            restrictedKeys.add(CUSTOMER_ID_KEY);
            restrictedKeys.add("identifier");
        }
        return restrictedKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkCustomerCriteria(final Criterion customerCriteria) {
        Assert.isTrue(canAccessToCustomer(CastUtils.toString(customerCriteria.getValue())),
                "customerId's criteria is not equal to the customerId from context");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addRestriction(final String key, final QueryDto criteria) {
        switch (key) {
            case "identifier" :
                final Optional<Criterion> criterionOpt = criteria.find("identifier");
                if (criterionOpt.isPresent()) {
                    checkIdentifierCriteria(criterionOpt.get());
                }
                else {
                    criteria.addCriterion(getIdentifierRestriction());
                }
                break;
            default :
                throw new NotImplementedException("Restriction not defined for key: " + key);
        }
    }

    private Criterion getIdentifierRestriction() {
        return new Criterion("identifier", externalSecurityService.getTenantIdentifier(), CriterionOperator.EQUALS);
    }

    /**
     * Method allowing to check the content of the criterion for the field "identifier"
     * @param identifierCriterion Criterion linked to the identifier to check.
     */
    protected void checkIdentifierCriteria(final Criterion identifierCriterion) {
        final List<Integer> identifiers = new ArrayList<>();
        switch (identifierCriterion.getOperator()) {
            case EQUALS :
                identifiers.add(CastUtils.toInteger(identifierCriterion.getValue()));
                break;
            case IN :
                identifiers.addAll(CastUtils.toList(identifierCriterion.getValue()));
                break;
            default :
                throw new IllegalArgumentException("Operation " + identifierCriterion.getOperator() + " is not supported for field : identifier");
        }
        final List<Integer> wrongIdentifiers = identifiers.stream().filter(identifier -> canAccessToTenant(identifier)).collect(Collectors.toList());
        if (!wrongIdentifiers.isEmpty()) {
            throw new NoRightsException(String.format(TENANT_INSUFFICIENT_PERMISSION_MESSAGE, wrongIdentifiers.toString()));
        }
    }

    @Override
    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public void checkLogbookRight(final String id) {
        final boolean hasRoleGetTenant = externalSecurityService.hasRole(ServicesData.ROLE_GET_TENANTS);
        if (!hasRoleGetTenant) {
            if (!StringUtils.equals(externalSecurityService.getCurrentTenantDto().getId(), id)) {
                throw new ForbiddenException(String.format("Unable to access tenant with id: %s", id));
            }

        }
        final TenantDto tenantDto = super.getOne(id);
        if (tenantDto == null) {
            throw new ForbiddenException(String.format("Unable to access tenant with id: %s", id));
        }
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

    @Override
    protected TenantInternalRestClient getClient() {
        return tenantInternalRestClient;
    }
}
