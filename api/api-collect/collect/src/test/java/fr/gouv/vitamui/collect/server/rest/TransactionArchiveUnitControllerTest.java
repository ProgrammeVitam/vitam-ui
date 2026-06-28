/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.collect.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.server.service.TransactionArchiveUnitService;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_ARCHIVE_UNITS_PATH;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TransactionArchiveUnitControllerTest extends ApiCollectControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionArchiveUnitControllerTest.class);

    private static final String ANY_TRANSACTION_CODE = "ANY_TRANSACTION_CODE";

    @Mock
    private TransactionArchiveUnitService transactionArchiveUnitService;

    @Mock
    private ExternalParametersService externalParametersService;

    @Mock
    private SignedDownloadTokenService signedDownloadTokenService;

    private TransactionArchiveUnitController transactionArchiveUnitController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionArchiveUnitController = new TransactionArchiveUnitController(
            transactionArchiveUnitService,
            externalParametersService,
            securityService,
            new ObjectMapper(),
            signedDownloadTokenService
        );
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.PROJECTS };
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return COLLECT_TRANSACTION_ARCHIVE_UNITS_PATH;
    }

    @Test
    void test_searchArchiveUnitsByCriteria_with_invalid_criteria_should_return_ko()
        throws InvalidCreateOperationException, VitamClientException, InvalidParseOperationException, JacksonException {
        SearchCriteriaDto query = new SearchCriteriaDto();
        SearchCriteriaEltDto nodeCriteria = new SearchCriteriaEltDto();
        nodeCriteria.setCriteria("NODES");
        nodeCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        nodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        nodeCriteria.setValues(List.of(new CriteriaValue("<s>insecure</s>")));
        query.setCriteriaList(List.of(nodeCriteria));
        VitamUIArchiveUnitResponseDto expectedResponse = new VitamUIArchiveUnitResponseDto();

        assertThatCode(() -> transactionArchiveUnitController.searchArchiveUnits("transactionId", query))
            .isInstanceOf(PreconditionFailedException.class)
            .hasMessage("The object is not valid ");
    }

    @Test
    void test_searchArchiveUnitsByCriteria_with_valid_criteria_should_return_ok()
        throws InvalidParseOperationException, PreconditionFailedException, InvalidCreateOperationException, VitamClientException, JacksonException {
        SearchCriteriaDto query = new SearchCriteriaDto();
        VitamUIArchiveUnitResponseDto expectedResponse = new VitamUIArchiveUnitResponseDto();
        Mockito.when(
            transactionArchiveUnitService.searchArchiveUnitsByCriteria(eq("transactionId"), eq(query), any())
        ).thenReturn(expectedResponse);
        VitamUIArchiveUnitResponseDto responseDto = transactionArchiveUnitController.searchArchiveUnits(
            "transactionId",
            query
        );
        Assertions.assertEquals(expectedResponse, responseDto);
    }

    @Test
    void testGetOntologiesListThenReturnOntologiesValuesList() throws PreconditionFailedException, IOException {
        // Given
        List<VitamUiOntologyDto> expectedResponse = new ArrayList<>();

        // When
        Mockito.when(securityService.getTenantIdentifier()).thenReturn(42);
        Mockito.when(transactionArchiveUnitService.readExternalOntologiesFromFile(eq(42))).thenReturn(expectedResponse);
        List<VitamUiOntologyDto> response = transactionArchiveUnitController.getExternalOntologiesList();

        // Then
        Assertions.assertEquals(response, expectedResponse);
    }

    @Test
    void testSelectUnitWithInheritedRulesThenReturnVitamOperationId()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException, IOException {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        ResultsDto expectedResponse = new ResultsDto();

        // When
        Mockito.when(externalParametersService.buildVitamContextFromExternalParam()).thenReturn(new VitamContext(0));
        Mockito.when(
            transactionArchiveUnitService.selectUnitWithInheritedRules(
                any(SearchCriteriaDto.class),
                eq(ANY_TRANSACTION_CODE),
                any(VitamContext.class)
            )
        ).thenReturn(expectedResponse);
        ResultsDto response = transactionArchiveUnitController.selectUnitWithInheritedRules(
            ANY_TRANSACTION_CODE,
            searchCriteriaDto
        );

        // Then
        Assertions.assertEquals(response, expectedResponse);
    }
}
