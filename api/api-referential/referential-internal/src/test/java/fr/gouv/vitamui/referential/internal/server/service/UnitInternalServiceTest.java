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
package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.referential.internal.server.unit.UnitInternalService;
import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.mock;

public class UnitInternalServiceTest {

    private UnitService unitService;
    private UnitInternalService unitInternalService;

    public final String FILLING_HOLDING_SCHEME_QUERY = "data/fillingholding/expected_unitType_query.json";

    @Before
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        unitService = mock(UnitService.class);
        unitInternalService = new UnitInternalService(unitService);
    }

    @Test
    public void searchUnits_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<JsonNode>().setHttpCode(200)
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnits_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<JsonNode>().setHttpCode(400)
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnits_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void searchUnitsWithErrors_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnitsWithErrors_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnitsWithErrors_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findUnitById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<JsonNode>().setHttpCode(200)
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findUnitById(unitId, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findUnitById_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class))).andReturn(
            new RequestResponseOK<JsonNode>().setHttpCode(400)
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findUnitById(unitId, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findUnitById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class))).andThrow(
            new VitamClientException("Exception thrown by vitam")
        );
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findUnitById(unitId, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findObjectMetadataById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findObjectMetadataById(unitId, dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findObjectMetadataById_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findObjectMetadataById(unitId, dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findObjectMetadataById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(
            unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class))
        ).andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findObjectMetadataById(unitId, dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void getFinalFillingHoldingSchemeQueryWithAllProjectionFields() throws Exception {
        // Given
        JsonNode expectedQuery = JsonHandler.getFromFile(PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_QUERY));

        // When
        JsonNode givenQuery = unitInternalService.createQueryForFillingOrHoldingUnit();

        // Then
        Assertions.assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#object")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#unitType")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#id")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("DescriptionLevel")
        ).isTrue();
    }
}
