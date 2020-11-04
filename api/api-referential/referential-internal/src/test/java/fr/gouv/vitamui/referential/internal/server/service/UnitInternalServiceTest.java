package fr.gouv.vitamui.referential.internal.server.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.mock;

import java.util.Optional;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.referential.internal.server.unit.UnitInternalService;

public class UnitInternalServiceTest {

    private UnitService unitService;
    private UnitInternalService unitInternalService;

    @Before
    public void setUp() {
        unitService = mock(UnitService.class);
        unitInternalService = new UnitInternalService(unitService);
    }

    @Test
    public void searchUnits_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnits_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnits_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnits(isA(JsonNode.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnits(dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void searchUnitsWithErrors_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnitsWithErrors_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void searchUnitsWithErrors_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.searchUnitsWithErrors(isA(Optional.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.searchUnitsWithErrors(Optional.empty(), dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findUnitById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findUnitById(unitId, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findUnitById_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findUnitById(unitId, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findUnitById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";

        expect(unitService.findUnitById(isA(String.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
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

        expect(unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(200));
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

        expect(unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<JsonNode>().setHttpCode(400));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findObjectMetadataById(unitId, dslQuery, vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findObjectMetadataById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String unitId = "id";
        JsonNode dslQuery = JsonHandler.createObjectNode();

        expect(unitService.findObjectMetadataById(isA(String.class), isA(JsonNode.class), isA(VitamContext.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(unitService);

        assertThatCode(() -> {
            unitInternalService.findObjectMetadataById(unitId, dslQuery, vitamContext);
        }).isInstanceOf(VitamClientException.class);
    } 
}

