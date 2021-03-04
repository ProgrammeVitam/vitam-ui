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


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ArchiveSearchInternalServiceTest {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalServiceTest.class);

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "agencyService")
    private AgencyService agencyService;

    @InjectMocks
    private ArchiveSearchInternalService archiveSearchInternalService;

    public final String FILING_HOLDING_SCHEME_RESULTS = "data/vitam_filing_holding_units_response.json";

    @Before
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchInternalService = new ArchiveSearchInternalService(objectMapper, unitService, agencyService);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void when_vitamArchiveQueryService_throw_invalid_exception()
        throws InvalidParseOperationException, InvalidCreateOperationException {
        Map<String, List<String>> searchCriteriaMap = new HashMap<>();
        VitamQueryHelper
            .createQueryDSL(null, List.of("Test"), searchCriteriaMap, 0, 10, Optional.empty(),
                Optional.empty());
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() throws VitamClientException,
        IOException, InvalidParseOperationException {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(FILING_HOLDING_SCHEME_RESULTS));
        // When
        JsonNode jsonNode = archiveSearchInternalService.searchUnits(any(), any());

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

}
