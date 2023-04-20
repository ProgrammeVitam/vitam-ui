/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archives.search.common.common.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitCsv;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.dtos.ExportSearchResultParam;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;
import fr.gouv.vitamui.commons.api.exception.RequestEntityTooLargeException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.cleanString;

/**
 * Archive-Search export Internal service.
 */
@Service
public class ArchiveSearchUnitExportCsvInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchUnitExportCsvInternalService.class);

    private static final Integer EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS = 10000;
    public static final String FILING_UNIT = "FILING_UNIT";
    public static final String HOLDING_UNIT = "HOLDING_UNIT";
    private static final String INGEST_ARCHIVE_TYPE = "INGEST";

    private final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;
    private final ArchiveSearchInternalService archiveSearchInternalService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ArchiveSearchUnitExportCsvInternalService(
        final @Lazy ArchiveSearchInternalService archiveSearchInternalService,
        final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService,
        final ObjectMapper objectMapper) {
        this.archiveSearchAgenciesInternalService = archiveSearchAgenciesInternalService;
        this.archiveSearchInternalService = archiveSearchInternalService;
        this.objectMapper = objectMapper;

    }

    /**
     * Export archive unit by criteria into csv file
     *
     * @param searchQuery
     * @param vitamContext
     * @throws VitamClientException
     * @throws IOException
     */
    public Resource exportToCsvSearchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        LOGGER.info("Calling exportToCsvSearchArchiveUnitsByCriteria with query {} ", searchQuery);
        Locale locale = Locale.FRENCH;
        if (Locale.FRENCH.getLanguage().equals(searchQuery.getLanguage()) ||
            Locale.ENGLISH.getLanguage().equals(searchQuery.getLanguage())) {
            locale = Locale.forLanguageTag(searchQuery.getLanguage());
        }
        ExportSearchResultParam exportSearchResultParam = new ExportSearchResultParam(locale);
        return exportToCsvSearchArchiveUnitsByCriteriaAndParams(searchQuery, exportSearchResultParam, vitamContext);
    }


    /**
     * export ToCsv Search ArchiveUnits By Criteria And Params by language
     *
     * @param searchQuery
     * @param exportSearchResultParam
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    private Resource exportToCsvSearchArchiveUnitsByCriteriaAndParams(final SearchCriteriaDto searchQuery, final
    ExportSearchResultParam exportSearchResultParam, final VitamContext vitamContext)
        throws VitamClientException {
        try {
            archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            List<ArchiveUnitCsv> unitCsvList = exportArchiveUnitsByCriteriaToCsvFile(searchQuery, vitamContext);
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());
            // header record
            String[] headerRecordFr =
                exportSearchResultParam.getHeaders().toArray(new String[exportSearchResultParam.getHeaders().size()]);
            SimpleDateFormat dateFormat = new SimpleDateFormat(exportSearchResultParam.getPatternDate());
            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(exportSearchResultParam.getSeparator())
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();
            // write header record
            csvWriter.writeNext(headerRecordFr);

            // write data records
            unitCsvList.stream().forEach(archiveUnitCsv -> {
                String startDt = null;
                String endDt = null;
                if (archiveUnitCsv.getStartDate() != null) {
                    try {
                        startDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getStartDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing starting date {} ", archiveUnitCsv.getStartDate());
                    }
                }
                if (archiveUnitCsv.getEndDate() != null) {
                    try {
                        endDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getEndDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing end date {} ", archiveUnitCsv.getEndDate());
                    }
                }
                csvWriter.writeNext(new String[] {archiveUnitCsv.getId(), archiveUnitCsv.getArchiveUnitType(),
                    archiveUnitCsv.getOriginatingAgencyName(),
                    exportSearchResultParam.getDescriptionLevelMap().get(archiveUnitCsv.getDescriptionLevel()),
                    archiveUnitCsv.getTitle(),
                    startDt, endDt,
                    archiveUnitCsv.getDescription()});
            });
            // close writers
            csvWriter.close();
            writer.close();
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export csv file ", ex);
        }
    }


    private List<ArchiveUnitCsv> exportArchiveUnitsByCriteriaToCsvFile(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.info("Calling exporting  export ArchiveUnits to CSV with criteria {}", searchQuery);
            checkSizeLimit(vitamContext, searchQuery);
            searchQuery.setPageNumber(0);
            searchQuery.setSize(EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            JsonNode archiveUnitsResult =
                archiveSearchInternalService
                    .searchArchiveUnits(archiveSearchInternalService.mapRequestToDslQuery(searchQuery), vitamContext);
            final VitamUISearchResponseDto archivesResponse =
                objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
            LOGGER.info("archivesResponse found {} ", archivesResponse.getResults().size());
            Set<String> originesAgenciesCodes = archivesResponse.getResults().stream().map(
                    ResultsDto::getOriginatingAgency).
                filter(Objects::nonNull).collect(Collectors.toSet());

            List<AgencyModelDto> originAgenciesFound =
                archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(vitamContext, originesAgenciesCodes);
            Map<String, AgencyModelDto> agenciesMapByIdentifier =
                originAgenciesFound.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));
            return archivesResponse.getResults().stream().map(
                    archiveUnit -> RulesUpdateCommonService
                        .fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
                ).map(archiveUnit -> cleanAndMapArchiveUnitResult(archiveUnit, searchQuery.getLanguage()))
                .collect(Collectors.toList());

        } catch (IOException e) {
            LOGGER.error("Can't parse criteria as Vitam query {} : ", e);
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
    }

    /**
     * check limit of results limit
     *
     * @param vitamContext
     * @param searchQuery
     */
    private void checkSizeLimit(VitamContext vitamContext, SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        SearchCriteriaDto searchQueryCounting = new SearchCriteriaDto();
        searchQueryCounting.setCriteriaList(searchQuery.getCriteriaList());
        JsonNode archiveUnitsResult =
            archiveSearchInternalService
                .searchArchiveUnits(archiveSearchInternalService.mapRequestToDslQuery(searchQueryCounting),
                    vitamContext);
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
        Integer nbResults = archivesOriginResponse.getHits().getTotal();
        if (nbResults >= EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS) {
            LOGGER.error("The archives units result found is greater than allowed {} ",
                EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            throw new RequestEntityTooLargeException(
                "The archives units result found is greater than allowed:  " + EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
        }
    }


    private ArchiveUnitCsv cleanAndMapArchiveUnitResult(ArchiveUnit archiveUnit, String language) {
        if (archiveUnit == null) {
            return null;
        }
        ArchiveUnitCsv archiveUnitCsv = new ArchiveUnitCsv();
        BeanUtils.copyProperties(archiveUnit, archiveUnitCsv);
        archiveUnitCsv.setDescription(
            archiveUnit.getDescription() != null ? cleanString(archiveUnit.getDescription()) : null);
        archiveUnitCsv.setDescriptionLevel(
            archiveUnit.getDescriptionLevel() != null ? cleanString(archiveUnit.getDescriptionLevel()) : null);
        archiveUnitCsv.setArchiveUnitType(getArchiveUnitType(archiveUnit, language));
        archiveUnitCsv.setTitle(cleanString(getArchiveUnitTitle(archiveUnit)));
        archiveUnitCsv.setOriginatingAgencyName(
            archiveUnit.getOriginatingAgencyName() != null ? cleanString(archiveUnit.getOriginatingAgencyName()) :
                null);
        return archiveUnitCsv;
    }

    private String getArchiveUnitTitle(ArchiveUnit archiveUnit) {
        String title = null;
        if (archiveUnit != null) {
            if (StringUtils.isEmpty(archiveUnit.getTitle()) || StringUtils.isBlank(archiveUnit.getTitle())) {
                if (archiveUnit.getTitle_() != null) {
                    if (!StringUtils.isEmpty(archiveUnit.getTitle_().getFr()) &&
                        !StringUtils.isBlank(archiveUnit.getTitle_().getFr())) {
                        title = archiveUnit.getTitle_().getFr();
                    } else {
                        title = archiveUnit.getTitle_().getEn();
                    }
                }
            } else {
                title = archiveUnit.getTitle();
            }
        }
        return title;
    }

    private String getArchiveUnitType(ArchiveUnit archiveUnit, String language) {
        String archiveUnitType = null;
        if (archiveUnit != null && !StringUtils.isEmpty(archiveUnit.getUnitType())) {
            switch (archiveUnit.getUnitType()) {
                case FILING_UNIT:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                        ExportSearchResultParam.FR_AU_FILING_SCHEME : ExportSearchResultParam.EN_AU_FILING_SCHEME;
                    break;
                case HOLDING_UNIT:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                        ExportSearchResultParam.FR_AU_HOLDING_SCHEME : ExportSearchResultParam.EN_AU_HOLDING_SCHEME;
                    break;
                case INGEST_ARCHIVE_TYPE:
                    if (StringUtils.isEmpty(archiveUnit.getUnitObject())) {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                            ExportSearchResultParam.FR_AU_WITHOUT_OBJECT : ExportSearchResultParam.EN_AU_WITHOUT_OBJECT;
                    } else {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage()) ?
                            ExportSearchResultParam.FR_AU_WITH_OBJECT : ExportSearchResultParam.EN_AU_WITH_OBJECT;
                    }
                    break;
                default:
                    throw new InvalidTypeException("Description Level Type is Unknown !");
            }
        }
        return archiveUnitType;
    }
}
