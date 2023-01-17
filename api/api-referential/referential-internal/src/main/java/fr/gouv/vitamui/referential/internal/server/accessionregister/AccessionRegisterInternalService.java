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
package fr.gouv.vitamui.referential.internal.server.accessionregister;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.QueryProjection;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessionRegisterDetailModel;
import fr.gouv.vitam.common.model.administration.AccessionRegisterSummaryModel;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.model.HitsDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterCsv;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailResponseDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterStatsDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryResponseDto;
import fr.gouv.vitamui.referential.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.referential.common.dto.ExportAccessionRegisterResultParam;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AccessionRegisterInternalService {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(AccessionRegisterInternalService.class);

    private final ObjectMapper objectMapper;

    private final AgencyService agencyService;

    private final AdminExternalClient adminExternalClient;

    private final AccessionRegisterService accessionRegisterService;

    @Autowired
    public AccessionRegisterInternalService(ObjectMapper objectMapper, AdminExternalClient adminExternalClient,
        AgencyService agencyService, AccessionRegisterService accessionRegisterService) {
        this.objectMapper = objectMapper;
        this.agencyService = agencyService;
        this.adminExternalClient = adminExternalClient;
        this.accessionRegisterService = accessionRegisterService;
    }

    public List<AccessionRegisterSummaryDto> getAll(VitamContext context) {
        RequestResponse<AccessionRegisterSummaryModel> requestResponse;
        try {
            LOGGER.debug("List of Accession Register EvIdAppSession : {} ", context.getApplicationSessionId());
            requestResponse = accessionRegisterService.findAccessionRegisterSummary(context);
            final AccessionRegisterSummaryResponseDto accessionRegisterSymbolicResponseDto = objectMapper
                .treeToValue(requestResponse.toJsonNode(), AccessionRegisterSummaryResponseDto.class);
            return AccessionRegisterConverter.toSummaryDtos(accessionRegisterSymbolicResponseDto.getResults());
        } catch (JsonProcessingException | VitamClientException e) {
            throw new InternalServerException("Unable to find accessionRegisterSymbolic", e);
        }
    }

    public PaginatedValuesDto<AccessionRegisterDetailDto> getAllPaginated(
        Optional<String> criteria,
        final Integer pageNumber,
        final Integer pageSize,
        final String orderBy,
        final DirectionDto direction,
        VitamContext vitamContext) {

        //Constructing json query for Vitam
        LOGGER.debug("List of Accession Registers EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        JsonNode query;
        try {
            AccessionRegisterSearchDto accessionRegisterSearchDto = criteria.isPresent() ?
                objectMapper.readValue(criteria.get(), AccessionRegisterSearchDto.class) :
                new AccessionRegisterSearchDto();
            query = AccessRegisterVitamQueryHelper.createQueryDSL(
                accessionRegisterSearchDto, pageNumber, pageSize, orderBy, direction);
        } catch (JsonProcessingException | InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get paginated accession registers", ioe);
        }
        //Fetching data from vitam
        AccessionRegisterDetailResponseDto results = fetchingAllPaginatedDataFromVitam(vitamContext, query);
        LOGGER.debug("Fetched accession register data : {} ", results);

        //Fetch agencies to complete return Dto 'originatingAgencyLabel' property
        Map<String, String> agenciesMap = findAgencies(vitamContext, results);

        HitsDto hits = results.getHits();
        Integer resultSize = 0;
        Integer resultTotal = 0;
        if (hits != null) {
            resultSize = hits.getSize();
            resultTotal = hits.getTotal();
        }

        boolean hasMore = pageNumber * pageSize + resultSize < resultTotal;
        List<AccessionRegisterDetailDto> valuesDto = AccessionRegisterConverter.toDetailsDtos(results.getResults());
        valuesDto.forEach(value -> {
            value.setOriginatingAgencyLabel(agenciesMap.get(value.getOriginatingAgency()));
            value.setSubmissionAgencyLabel(agenciesMap.get(value.getSubmissionAgency()));
        });

        //Build statistique datas
        Map<String, Object> optionalValues = new HashMap<>();
        AccessionRegisterStatsDto statsDto = AccessRegisterStatsHelper.fetchStats(results.getResults());
        optionalValues.put("stats", statsDto);

        return new PaginatedValuesDto<>(valuesDto, pageNumber, pageSize, resultTotal, hasMore, optionalValues);
    }

    /**
     * Export accession register details into csv result file
     *
     * @param searchQuery
     * @param vitamContext
     * @throws VitamClientException
     * @throws IOException
     */
    public Resource exportToCsvAccessionRegister(final AccessionRegisterSearchDto searchQuery,
        final VitamContext vitamContext) {
        LOGGER.debug("Calling exportToCsvAccessionRegister with query {} ", searchQuery);
        Locale locale = Locale.FRENCH;
        // add language to query model
        ExportAccessionRegisterResultParam exportSearchResultParam = new ExportAccessionRegisterResultParam(locale);
        return exportAccessionRegistersByCriteriaAndParams(searchQuery, exportSearchResultParam, vitamContext);
    }

    /**
     * export to csv Accession Register By Criteria And Params by language
     *
     * @param searchQuery
     * @param exportAccessionRegisterResultParam
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    private Resource exportAccessionRegistersByCriteriaAndParams(final AccessionRegisterSearchDto searchQuery,
        final ExportAccessionRegisterResultParam exportAccessionRegisterResultParam, final VitamContext vitamContext) {
        try {

            List<AccessionRegisterCsv> accessionRegisterCsvList =
                exportAccessionRegisterToCsvFile(searchQuery, vitamContext);
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());
            // header record
            String[] headerRecordFr =
                exportAccessionRegisterResultParam.getHeaders()
                    .toArray(new String[exportAccessionRegisterResultParam.getHeaders().size()]);
            SimpleDateFormat dateFormat = new SimpleDateFormat(exportAccessionRegisterResultParam.getPatternDate());
            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(exportAccessionRegisterResultParam.getSeparator())
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();
            // write header record
            csvWriter.writeNext(headerRecordFr);

            // write data records
            accessionRegisterCsvList.stream().forEach(accessionRegisterCsv -> {
                String startDateFormated = null;
                if (accessionRegisterCsv.getStartDate() != null) {
                    try {
                        startDateFormated =
                            dateFormat.format(LocalDateUtil.getDate(accessionRegisterCsv.getStartDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing starting date {} ", accessionRegisterCsv.getStartDate());
                    }
                }

                csvWriter.writeNext(new String[] {
                    accessionRegisterCsv.getOpi(),
                    startDateFormated, //dateEntree
                    accessionRegisterCsv.getOriginatingAgency(), //servProd
                    accessionRegisterCsv.getSubmissionAgency(), //servVers
                    accessionRegisterCsv.getArchivalAgreement(), //contratEntree
                    accessionRegisterCsv.getAcquisitionInformation(), //modeEntree
                    accessionRegisterCsv.getLegalStatus(), //statutJur
                    String.valueOf(accessionRegisterCsv.getTotalUnits().getIngested()),
                    String.valueOf(accessionRegisterCsv.getTotalObjectsGroups().getIngested()),
                    String.valueOf(accessionRegisterCsv.getTotalObjects().getIngested()),
                    VitamUIUtils.humanReadableByteCountBin(accessionRegisterCsv.getObjectSize().getIngested()),
                    accessionRegisterCsv.getStatus().value()
                });
            });
            // close writers
            csvWriter.close();
            writer.close();
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export csv file ", ex);
        }
    }

    private List<AccessionRegisterCsv> exportAccessionRegisterToCsvFile(final AccessionRegisterSearchDto searchQuery,
        final VitamContext vitamContext) {
        try {
            JsonNode query = AccessRegisterVitamQueryHelper.createQueryDSL(searchQuery);
            LOGGER.debug("Final query details: {}", query.toPrettyString());
            //Fetching data from vitam
            AccessionRegisterDetailResponseDto accessionRegisterDetailResponseDto =
                fetchingAllPaginatedDataFromVitam(vitamContext, query);
            LOGGER.debug("Fetched accession register data : {} ", accessionRegisterDetailResponseDto);
            List<AccessionRegisterCsv> accessionRegisterList = new ArrayList<>();
            if (accessionRegisterDetailResponseDto != null) {
                accessionRegisterList =
                    accessionRegisterDetailResponseDto.getResults().stream().map(this::fillOriginatingAgencyName)
                        .collect(Collectors.toList());
            }
            return accessionRegisterList;
        } catch (InvalidCreateOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        } catch (InvalidParseOperationException ioe) {
            throw new InternalServerException("Can't create dsl query to get ordered accession registers", ioe);
        }
    }

    private AccessionRegisterCsv fillOriginatingAgencyName(AccessionRegisterDetailModel accessionRegister) {
        AccessionRegisterCsv accessionRegisterCsv = new AccessionRegisterCsv();
        BeanUtils.copyProperties(accessionRegister, accessionRegisterCsv);
        return accessionRegisterCsv;
    }

    private AccessionRegisterDetailResponseDto fetchingAllPaginatedDataFromVitam(VitamContext vitamContext,
        JsonNode query) {
        AccessionRegisterDetailResponseDto results;
        try {
            RequestResponse<AccessionRegisterDetailModel> accessionRegisterDetails =
                adminExternalClient.findAccessionRegisterDetails(vitamContext, query);
            results = objectMapper.treeToValue(accessionRegisterDetails.toJsonNode(),
                AccessionRegisterDetailResponseDto.class);
        } catch (VitamClientException e) {
            throw new InternalServerException("Can't fetch data from VITAM", e);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Can't process Json Parsing", e);
        }
        return results;
    }

    private Map<String, String> findAgencies(VitamContext vitamContext, AccessionRegisterDetailResponseDto results) {

        JsonNode agencyQuery;
        List<AgencyModelDto> agencies;
        try {
            agencyQuery = buildAgencyProjectionQuery(results);
            RequestResponse<AgenciesModel> requestResponse =
                agencyService.findAgencies(vitamContext, agencyQuery);
            agencies = objectMapper.treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Error parsing query", e);
        } catch (VitamClientException e) {
            throw new InternalServerException("Error fetching agencies from vitam", e);
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Invalid Select vitam query", e);
        }

        return agencies.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, AgencyModelDto::getName));
    }

    private JsonNode buildAgencyProjectionQuery(AccessionRegisterDetailResponseDto results)
        throws InvalidCreateOperationException {

        List<String> distinctOriginatingAgencies = new ArrayList<>();
        if (results != null) {
            distinctOriginatingAgencies = results.getResults().stream()
                .map(AccessionRegisterDetailModel::getOriginatingAgency)
                .filter(Objects::nonNull)
                .filter(originatingAgency -> ConcurrentHashMap.newKeySet().add(originatingAgency))
                .collect(Collectors.toList());

            distinctOriginatingAgencies.addAll(results.getResults().stream()
                .map(AccessionRegisterDetailModel::getSubmissionAgency)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        }

        final Select select = new Select();

        select.setQuery(QueryHelper.in("Identifier", distinctOriginatingAgencies.toArray(new String[0])));
        Map<String, Integer> projection = new HashMap<>();
        projection.put("Identifier", 1);
        projection.put("Name", 1);

        QueryProjection queryProjection = new QueryProjection();
        queryProjection.setFields(projection);
        try {
            select.setProjection(JsonHandler.toJsonNode(queryProjection));
        } catch (InvalidParseOperationException e) {
            throw new InvalidCreateOperationException("Invalid vitam query", e);
        }
        LOGGER.debug("agencies query: {}", select.getFinalSelect());
        return select.getFinalSelect();
    }

}
