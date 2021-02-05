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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.client.CustomerInternalRestClient;
import fr.gouv.vitamui.iam.internal.client.CustomerInternalWebClient;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the customers.
 *
 *
 */
@Getter
@Setter
@Service
public class CustomerExternalService extends AbstractResourceClientService<CustomerDto, CustomerDto> {

    private final CustomerInternalRestClient customerInternalRestClient;

    private final CustomerInternalWebClient customerInternalV2RestClient;

    @Autowired
    public CustomerExternalService(final CustomerInternalRestClient customerInternalRestClient, final CustomerInternalWebClient customerInternalV2RestClient,
            final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.customerInternalRestClient = customerInternalRestClient;
        this.customerInternalV2RestClient = customerInternalV2RestClient;
    }

    public CustomerDto create(final CustomerCreationFormData customerData) {
        return customerInternalV2RestClient.create(getInternalHttpContext(), customerData);
    }

    public CustomerDto patch(final CustomerPatchFormData customerData) {
        return customerInternalV2RestClient.patch(getInternalHttpContext(), customerData);
    }

    @Override
    public CustomerDto update(final CustomerDto dto) {
        return super.update(dto);
    }

    @Override
    public CustomerDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    @Override
    public CustomerDto getOne(final String id) {
        return super.getOne(id);
    }

    @Override
    public List<CustomerDto> getAll(final Optional<String> criteria) {
        return super.getAll(criteria);
    }

    @Override
    public boolean checkExists(final String criteria) {
        return super.checkExists(criteria);
    }

    @Override
    public PaginatedValuesDto<CustomerDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    public CustomerDto getByCode(final String code) {
        return getClient().getByCode(getInternalHttpContext(), code);
    }

    public CustomerDto getMyCustomer() {
        return getClient().getMyCustomer(getInternalHttpContext());
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return Arrays.asList("id", "name", "code", "companyName", "enabled", "language", "otp", "defaultEmailDomain", "emailDomains", "subrogeable");
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return Collections.emptyList();
    }

    @Override
    protected CustomerInternalRestClient getClient() {
        return customerInternalRestClient;
    }

    @Override
    protected String getVersionApiCrtieria() {
        return CRITERIA_VERSION_V2;
    }

    @Override
    public JsonNode findHistoryById(final String id) {
        checkLogbookRight(id);
        return getClient().findHistoryById(getInternalHttpContext(), id);
    }

    public void checkLogbookRight(final String id) {
        final boolean hasRoleGetCustomers = externalSecurityService.hasRole(ServicesData.ROLE_GET_CUSTOMERS);
        if (!hasRoleGetCustomers) {
            if (!StringUtils.equals(externalSecurityService.getUser().getCustomerId(), id)) {
                throw new ForbiddenException(String.format("Unable to access customer with id: %s", id));
            }
        }
        final CustomerDto customerDto = super.getOne(id);
        if (customerDto == null) {
            throw new ForbiddenException(String.format("Unable to access customer with id: %s", id));
        }
    }

    public ResponseEntity<Resource> getLogo(final String id, final AttachmentType type) {
        return getClient().getLogo(getInternalHttpContext(), id, type);
    }

    public boolean getGdprSettingStatus() {
        return getClient().getGdprSettingStatus(getInternalHttpContext());
    }
}
