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
package fr.gouv.archive.internal.client;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

public class ArchiveInternalRestClient
    extends BasePaginatingAndSortingRestClient<ArchiveUnitsDto, InternalHttpContext> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveInternalRestClient.class);

    protected Class<JsonNode> getJsonNodeClass() {
        return JsonNode.class;
    }

    public ArchiveInternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    @Override
    protected Class<ArchiveUnitsDto> getDtoClass() {
        return ArchiveUnitsDto.class;
    }

    @Override
    protected ParameterizedTypeReference<List<ArchiveUnitsDto>> getDtoListClass() {
        return new ParameterizedTypeReference<List<ArchiveUnitsDto>>() {
        };
    }

    @Override
    protected ParameterizedTypeReference<PaginatedValuesDto<ArchiveUnitsDto>> getDtoPaginatedClass() {
        return new ParameterizedTypeReference<>() {
        };
    }

    @Override
    public String getPathUrl() {
        return RestApi.ARCHIVE_SEARCH_PATH;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(InternalHttpContext context, SearchCriteriaDto query) {
        LOGGER.debug("Calling searchArchiveUnitsByCriteria with query {} ", query);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<ArchiveUnitsDto> response =
            restTemplate.exchange(getUrl() + RestApi.SEARCH_PATH, HttpMethod.POST,
                request, ArchiveUnitsDto.class);
        checkResponse(response);
        return response.getBody();
    }


    public VitamUISearchResponseDto getFilingHoldingScheme(InternalHttpContext context) {
        LOGGER.debug("Calling get filing holding scheme");
        MultiValueMap<String, String> headers = buildHeaders(context);

        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<VitamUISearchResponseDto> response = restTemplate
            .exchange(getUrl() + RestApi.FILING_HOLDING_SCHEME_PATH, HttpMethod.GET, request,
                VitamUISearchResponseDto.class);
        checkResponse(response);
        return response.getBody();
    }

    public ResponseEntity<ResultsDto> findUnitById(String id, final InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }

    public ResponseEntity<ResultsDto> findObjectById(String id, final InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.OBJECTGROUP + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build(id), HttpMethod.GET, request, ResultsDto.class);
    }

    public Resource exportCsvArchiveUnitsByCriteria(final SearchCriteriaDto query,
        final InternalHttpContext context) {
        LOGGER.debug("Calling exportCsvArchiveUnitsByCriteria with query {} ", query);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<Resource> response =
            restTemplate.exchange(getUrl() + RestApi.EXPORT_CSV_SEARCH_PATH, HttpMethod.POST,
                request, Resource.class);
        checkResponse(response);
        return response.getBody();

    }

    public String exportDIPByCriteria(final ExportDipCriteriaDto exportDipCriteriaDto,
        final InternalHttpContext context) {
        LOGGER.debug("Calling exportDIPByCriteria with query {} ", exportDipCriteriaDto);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<ExportDipCriteriaDto> request = new HttpEntity<>(exportDipCriteriaDto, headers);
        final ResponseEntity<String> response =
            restTemplate.exchange(getUrl() + RestApi.EXPORT_DIP, HttpMethod.POST, request, String.class);
        checkResponse(response);
        return response.getBody();
    }

    public String transferRequest(final TransferRequestDto transferRequestDto,
        final InternalHttpContext context) {
        LOGGER.debug("Calling transfer request with query {} ", transferRequestDto);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<TransferRequestDto> request = new HttpEntity<>(transferRequestDto, headers);
        final ResponseEntity<String> response =
            restTemplate.exchange(getUrl() + RestApi.TRANSFER_REQUEST, HttpMethod.POST, request, String.class);
        checkResponse(response);
        return response.getBody();
    }

    public ResponseEntity<JsonNode> startEliminationAnalysis(final InternalHttpContext context,
        final SearchCriteriaDto query) {
        LOGGER.debug("Calling elimination analysis with query {} ", query);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<JsonNode> response =
            restTemplate.exchange(getUrl() + RestApi.ELIMINATION_ANALYSIS, HttpMethod.POST,
                request, JsonNode.class);
        checkResponse(response);
        return response;

    }

    public ResponseEntity<JsonNode> startEliminationAction(final InternalHttpContext context,
        final SearchCriteriaDto query) {
        LOGGER.debug("Calling elimination action with query {} ", query);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<JsonNode> response =
            restTemplate.exchange(getUrl() + RestApi.ELIMINATION_ACTION, HttpMethod.POST,
                request, JsonNode.class);
        checkResponse(response);
        return response;

    }

    public String updateArchiveUnitsRules(final RuleSearchCriteriaDto ruleSearchCriteriaDto,
        final InternalHttpContext context) {
        LOGGER.debug("Calling Update Archive Units Rules  with query {} ", ruleSearchCriteriaDto);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<RuleSearchCriteriaDto> request = new HttpEntity<>(ruleSearchCriteriaDto, headers);
        final ResponseEntity<String> response =
            restTemplate.exchange(getUrl() + RestApi.MASS_UPDATE_UNITS_RULES, HttpMethod.POST,
                request, String.class);
        checkResponse(response);
        return response.getBody();

    }

    public String computedInheritedRules(final SearchCriteriaDto searchCriteriaDto,
        final InternalHttpContext context) {
        LOGGER.debug("Calling computedInheritedRules with query {} ", searchCriteriaDto);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(searchCriteriaDto, headers);
        final ResponseEntity<String> response =
            restTemplate.exchange(getUrl() + RestApi.COMPUTED_INHERITED_RULES, HttpMethod.POST,
                request, String.class);
        checkResponse(response);
        return response.getBody();

    }


    public ResultsDto selectUnitWithInheritedRules(InternalHttpContext context, SearchCriteriaDto query) {
        LOGGER.debug("Calling select Unit With Inherited Rules with query {} ", query);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<SearchCriteriaDto> request = new HttpEntity<>(query, headers);
        final ResponseEntity<ResultsDto> response =
            restTemplate.exchange(getUrl() + RestApi.UNIT_WITH_INHERITED_RULES, HttpMethod.POST,
                request, ResultsDto.class);
        checkResponse(response);
        return response.getBody();
    }

    public String reclassification(final ReclassificationCriteriaDto reclassificationCriteriaDto,
        final InternalHttpContext context) {
        LOGGER.debug("Calling reclassification with query {} ", reclassificationCriteriaDto);
        MultiValueMap<String, String> headers = buildHeaders(context);
        final HttpEntity<ReclassificationCriteriaDto> request = new HttpEntity<>(reclassificationCriteriaDto, headers);
        final ResponseEntity<String> response =
            restTemplate.exchange(getUrl() + RestApi.RECLASSIFICATION, HttpMethod.POST,
                request, String.class);
        checkResponse(response);
        return response.getBody();
    }

    public String updateUnitById(String id, UnitDescriptiveMetadataDto unitDescriptiveMetadataDto,
        InternalHttpContext context) {
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID);
        final HttpEntity<?> request = new HttpEntity<>(unitDescriptiveMetadataDto, buildHeaders(context));
        ResponseEntity<String> response =
            restTemplate.exchange(uriBuilder.build(id), HttpMethod.PUT, request, String.class);
        return response.getBody();
    }

    public List<VitamUiOntologyDto> getExternalOntologiesList(final InternalHttpContext context) {
        LOGGER.debug("[INTERNAL] : Calling Get External ontologies list");
        final UriComponentsBuilder uriBuilder =
            UriComponentsBuilder.fromHttpUrl(getUrl() + CommonConstants.EXTERNAL_ONTOLOGIES_LIST);
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET, request, ArrayList.class).getBody();
    }

    public PersistentIdentifierResponseDto findUnitsByPersistentIdentifier(String identifier, final InternalHttpContext context) {
        final HttpEntity<?> request = new HttpEntity<>(buildHeaders(context));
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.UNITS_PERSISTENT_IDENTIFIER).queryParam("id", identifier);
        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET, request, PersistentIdentifierResponseDto.class).getBody();
    }

}
