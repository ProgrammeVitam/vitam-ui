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
package fr.gouv.vitamui.iam.internal.client;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;

/**
 * Internal WebClient for Customer operations.
 *
 *
 */
public class CustomerInternalWebClient extends BaseWebClient<ExternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerInternalWebClient.class);

    public CustomerInternalWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    /**
     * Create a customer using a {@link CustomerCreationFormData} object. The logo is optional.
     * A {@link CustomerDto} object is provided in the data and a {@link MultipartFile} is there's a graphical identity for this customer.
     * @param context
     * @param customerCreationFormData
     * @return
     */
    public CustomerDto create(final InternalHttpContext context, final CustomerCreationFormData customerCreationFormData) {
        LOGGER.debug("Create {}", customerCreationFormData);
        if (customerCreationFormData == null) {
            throw new BadRequestException("Customer data not found.");
        }

        return multiparts(getUrl(), HttpMethod.POST, context,
                Map.of("customerDto", customerCreationFormData.getCustomerDto(), "tenantName", customerCreationFormData.getTenantName()),
                customerCreationFormData.getHeader().isPresent() ? Optional.of(new AbstractMap.SimpleEntry<>("header", customerCreationFormData.getHeader().get())) : Optional.empty(),
                customerCreationFormData.getFooter().isPresent() ? Optional.of(new AbstractMap.SimpleEntry<>("footer", customerCreationFormData.getFooter().get())) : Optional.empty(),
                customerCreationFormData.getPortal().isPresent() ?  Optional.of(new AbstractMap.SimpleEntry<>("portal", customerCreationFormData.getPortal().get())) : Optional.empty(),
                CustomerDto.class);

    }

    /**
     * Create a customer using a {@link CustomerDto} object and a Path to a image file is there's a graphical identity for this customer.
     * @param context
     * @param dto
     * @param multipartFile
     * @return
     */
    public CustomerDto create(final InternalHttpContext context, final CustomerDto dto, final Optional<Path> multipartFile) {
        if (multipartFile.isPresent()) {
            return multipartDataFromFile(getUrl(), HttpMethod.POST, context, Collections.singletonMap("customerDto", dto),
                    Optional.of(new AbstractMap.SimpleEntry<>("logo", multipartFile.get())), CustomerDto.class);
        }
        else {
            return multipartDataFromFile(getUrl(), HttpMethod.POST, context, Collections.singletonMap("customerDto", dto), Optional.empty(), CustomerDto.class);
        }
    }

    /**
     * Patch a customer using a {@link Map<String, Object>} object and a Path to a image file is there's a graphical identity for this customer.
     * @param context
     * @param customerPatchFormData
     * @return
     */
    public CustomerDto patch(final InternalHttpContext context, CustomerPatchFormData customerPatchFormData) {
        LOGGER.debug("Patch {}", customerPatchFormData);
        return multiparts(getUrl() + '/' + customerPatchFormData.getPartialCustomerDto().get("id"), HttpMethod.PATCH, context,
            Collections.singletonMap("partialCustomerDto", customerPatchFormData.getPartialCustomerDto()),
            customerPatchFormData.getHeader().isPresent() ? Optional.of(new AbstractMap.SimpleEntry<>("header", customerPatchFormData.getHeader().get())) : Optional.empty(),
            customerPatchFormData.getFooter().isPresent() ? Optional.of(new AbstractMap.SimpleEntry<>("footer", customerPatchFormData.getFooter().get())) : Optional.empty(),
            customerPatchFormData.getPortal().isPresent() ?  Optional.of(new AbstractMap.SimpleEntry<>("portal", customerPatchFormData.getPortal().get())) : Optional.empty(),
            CustomerDto.class);
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_CUSTOMERS_URL;
    }
}
