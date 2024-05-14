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
package fr.gouv.vitamui.iam.external.client;

import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * A REST client to check existence, read, create, update and delete an identity provider.
 *
 *
 */
public class IdentityProviderExternalRestClient extends BaseCrudRestClient<IdentityProviderDto, ExternalHttpContext> {

    public IdentityProviderExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_PROVIDERS_URL;
    }

    @Override
    protected Class<IdentityProviderDto> getDtoClass() {
        return IdentityProviderDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<IdentityProviderDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<IdentityProviderDto>>() {};
    }

    public IdentityProviderDto create(
        final ExternalHttpContext context,
        final MultipartFile keystore,
        final MultipartFile idpMetadata,
        final String provider
    ) {
        MultiValueMap<String, String> headers = buildHeaders(context);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        if (Objects.nonNull(keystore)) {
            bodyBuilder.part("keystore", keystore.getResource());
        }
        if (Objects.nonNull(idpMetadata)) {
            bodyBuilder.part("idpMetadata", idpMetadata.getResource());
        }
        bodyBuilder.part("provider", provider);

        final HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(bodyBuilder.build(), headers);
        final ResponseEntity<IdentityProviderDto> response = restTemplate.exchange(
            getUrl(),
            HttpMethod.POST,
            request,
            getDtoClass()
        );
        checkResponse(response, 200, 201);
        return response.getBody();
    }
}
