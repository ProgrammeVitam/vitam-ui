/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.collect.external.client;

import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.CollectProjectUploadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.PROJECTS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.STREAM_UPLOAD_PATH;

/**
 * A REST client to stream file to Collect API.
 */
public class CollectStreamingExternalRestClient
    extends BasePaginatingAndSortingRestClient<CollectProjectUploadDto, ExternalHttpContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectStreamingExternalRestClient.class);

    public CollectStreamingExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return RestApi.COLLECT_PATH;
    }

    @Override
    protected Class<CollectProjectUploadDto> getDtoClass() {
        return CollectProjectUploadDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<CollectProjectUploadDto>> getDtoListClass() {
        return new ParameterizedTypeReference<>() {};
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<CollectProjectUploadDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<>() {};
    }

    public ResponseEntity<Void> streamingUpload(
        final ExternalHttpContext context,
        String fileName,
        String transactionId,
        InputStream inputStream
    ) {
        LOGGER.debug("Calling upload using streaming process");
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + PROJECTS + STREAM_UPLOAD_PATH
        );

        final MultiValueMap<String, String> headersList = new HttpHeaders();
        headersList.addAll(buildHeaders(context));
        headersList.add(CommonConstants.X_ORIGINAL_FILENAME_HEADER, fileName);
        headersList.add(CommonConstants.X_TRANSACTION_ID_HEADER, transactionId);
        HttpHeaders headersParams = new HttpHeaders();
        headersParams.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headersParams.addAll(headersList);

        final HttpEntity<InputStreamResource> request = new HttpEntity<>(
            new InputStreamResource(inputStream),
            headersParams
        );
        final ResponseEntity<Void> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.POST,
            request,
            Void.class
        );
        LOGGER.debug("The response is {}", response.toString());
        return response;
    }
}
