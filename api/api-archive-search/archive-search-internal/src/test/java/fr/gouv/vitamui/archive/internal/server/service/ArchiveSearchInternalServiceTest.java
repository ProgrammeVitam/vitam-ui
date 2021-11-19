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
package fr.gouv.vitamui.archive.internal.server.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.service.RulesUpdateCommonService;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchInternalServiceTest {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalServiceTest.class);

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "agencyService")
    private AgencyService agencyService;

    @MockBean(name = "archiveSearchAgenciesInternalService")
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;

    @MockBean(name = "archiveSearchRulesInternalService")
    private ArchiveSearchRulesInternalService archiveSearchRulesInternalService;

    @MockBean(name = "archivesSearchFieldsQueryBuilderService")
    private ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;

    @MockBean(name = "archivesSearchAppraisalQueryBuilderService")
    private ArchivesSearchAppraisalQueryBuilderService archivesSearchAppraisalQueryBuilderService;
    @InjectMocks
    private ArchiveSearchInternalService archiveSearchInternalService;

    @MockBean(name = "exportDipV2Service")
    private ExportDipV2Service exportDipV2Service;

    @MockBean(name = "eliminationService")
    private EliminationService eliminationService;

    @MockBean(name="ruleOperationsConverter")
    private RuleOperationsConverter ruleOperationsConverter;

    @MockBean(name="rulesUpdateCommonService")
    private RulesUpdateCommonService rulesUpdateCommonService;

    public final String FILING_HOLDING_SCHEME_RESULTS = "data/vitam_filing_holding_units_response.json";
    public final String ELIMINATION_ANALYSIS_QUERY = "data/elimination/query.json";
    public final String ELIMINATION_ANALYSIS_FINAL_QUERY = "data/elimination/expected_query.json";
    public final String ELIMINATION_ANALYSIS_FINAL_RESPONSE = "data/elimination/elimination_analysis_response.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchInternalService =
            new ArchiveSearchInternalService(objectMapper, unitService, archiveSearchAgenciesInternalService,
                archiveSearchRulesInternalService, archivesSearchFieldsQueryBuilderService,
                exportDipV2Service, archivesSearchAppraisalQueryBuilderService, eliminationService, ruleOperationsConverter, rulesUpdateCommonService);
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() throws VitamClientException,
        IOException, InvalidParseOperationException {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(FILING_HOLDING_SCHEME_RESULTS));
        // When
        JsonNode jsonNode = archiveSearchInternalService.searchArchiveUnits(any(), any());

        // Configure the mapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        VitamUISearchResponseDto vitamUISearchResponseDto =
            objectMapper.treeToValue(jsonNode, VitamUISearchResponseDto.class);

        // Then
        Assertions.assertThat(vitamUISearchResponseDto).isNotNull();
        Assertions.assertThat(vitamUISearchResponseDto.getResults().size()).isEqualTo(20);
    }

    private RequestResponse<JsonNode> buildUnitMetadataResponse(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader()
            .getResourceAsStream(filename);
        return RequestResponseOK
            .getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
    }

    @Test
    public void getFinalEliminationConstructedQuery() throws Exception {
        JsonNode fromString = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_QUERY));
        EliminationRequestBody eliminationRequestBody2 =
            archiveSearchInternalService.getEliminationRequestBody(fromString);

        JsonNode resultExpected = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_FINAL_QUERY));
        Assertions.assertThat(eliminationRequestBody2.getDslRequest()).isEqualTo(resultExpected);
        Assertions.assertThat(resultExpected.get(BuilderToken.GLOBAL.THRESOLD.exactToken()).asDouble()).isEqualTo(10000);
    }
}
