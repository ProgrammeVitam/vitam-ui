/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.external.client;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

public class ProfileExternalRestClient extends BasePaginatingAndSortingRestClient<ProfileDto, ExternalHttpContext> {

    public ProfileExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<ProfileDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<PaginatedValuesDto<ProfileDto>>() {};
    }

    @Override
    public String getPathUrl() {
        return RestApi.PROFILES_URL;
    }

    @Override
    protected Class<ProfileDto> getDtoClass() {
        return ProfileDto.class;
    }

    protected ParameterizedTypeReference<List<ProfileDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ProfileDto>>() {};
    }

    public boolean check(ExternalHttpContext context, ProfileDto ProfileDto) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.PATH_CHECK);
        final HttpEntity<ProfileDto> request = new HttpEntity<>(ProfileDto, buildHeaders(context));
        final ResponseEntity<Boolean> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.POST,
            request,
            Boolean.class
        );
        return response.getStatusCode() == HttpStatus.OK;
    }

    public ResponseEntity<Resource> download(ExternalHttpContext context, String id) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + RestApi.DOWNLOAD_PROFILE + CommonConstants.PATH_ID
        );
        final HttpEntity<ProfileDto> request = new HttpEntity<>(null, buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, Resource.class);
    }

    public ResponseEntity<JsonNode> updateProfileFile(
        ExternalHttpContext context,
        String id,
        MultipartFile profileFile
    ) throws IOException {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + RestApi.UPDATE_PROFILE_FILE + CommonConstants.PATH_ID
        );

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("file", new FileSystemResource(profileFile.getBytes(), profileFile.getOriginalFilename()));
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(bodyMap, buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.PUT, request, JsonNode.class);
    }

    public ResponseEntity<JsonNode> updateProfile(ExternalHttpContext c, ProfileDto dto) {
        beforeUpdate(dto);
        ApiUtils.checkValidity(dto);
        final String dtoId = dto.getId();
        final HttpEntity<ProfileDto> request = new HttpEntity<>(dto, buildHeaders(c));
        final ResponseEntity<JsonNode> response = restTemplate.exchange(
            getUrl() + CommonConstants.PATH_ID,
            HttpMethod.PUT,
            request,
            JsonNode.class,
            dtoId
        );
        checkResponse(response);
        return response;
    }

    protected void beforeUpdate(final ProfileDto dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getId()), "The DTO identifier must be not null for update.");
        ApiUtils.checkValidity(dto);
    }

    public static class FileSystemResource extends ByteArrayResource {

        private String fileName;

        public FileSystemResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.fileName = filename;
        }

        public String getFilename() {
            return fileName;
        }

        public void setFilename(String fileName) {
            this.fileName = fileName;
        }
    }
}
