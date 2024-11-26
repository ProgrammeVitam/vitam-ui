package fr.gouv.vitamui.referential.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.referential.common.dto.AgencyCSVDto;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.StringWriter;
import java.util.List;

class AgencyConverterTest {

    @Test
    void convertDtoToVitam() {
        AgencyDto agencyDto = agencyDto();

        AgenciesModel agencyModel = AgencyConverter.convertDtoToVitam(agencyDto);

        AgenciesModel agencyModelExpected = agencyModel();
        Assertions.assertEquals(agencyModelExpected, agencyModel);
    }

    @Test
    void convertVitamToDto() {
        AgenciesModel agencyModel = agencyModel();

        AgencyDto agencyDto = AgencyConverter.convertVitamToDto(agencyModel);

        AgencyDto agencyDtoExpected = agencyDto();
        Assertions.assertEquals(agencyDtoExpected, agencyDto);
    }

    @Test
    void convertDtosToVitams() {
        List<AgencyDto> agencyDtos = List.of(agencyDto());

        List<AgenciesModel> agenciesModels = AgencyConverter.convertDtosToVitams(agencyDtos);

        List<AgenciesModel> agenciesModelsExpected = List.of(agencyModel());
        Assertions.assertEquals(agenciesModelsExpected, agenciesModels);
    }

    @Test
    void convertVitamsToDtos() {
        List<AgenciesModel> agenciesModels = List.of(agencyModel());

        List<AgencyDto> agencyDtos = AgencyConverter.convertVitamsToDtos(agenciesModels);

        List<AgencyDto> agencyDtosExpected = List.of(agencyDto());
        Assertions.assertEquals(agencyDtosExpected, agencyDtos);
    }

    @Test
    void convertDtoToCsvDto() {
        AgenciesModel agencyModel = agencyModel();

        AgencyCSVDto agencyCSVDto = AgencyConverter.convertDtoToCsvDto(agencyModel);

        AgencyCSVDto agencyCSVDtoExpected = agencyCSVDto();
        Assertions.assertEquals(agencyCSVDtoExpected, agencyCSVDto);
    }

    @Test
    void convertDtosToCsvDtos() {
        List<AgenciesModel> agenciesModels = List.of(agencyModel());

        List<AgencyCSVDto> agencyCSVDtos = AgencyConverter.convertDtosToCsvDtos(agenciesModels);

        List<AgencyCSVDto> agencyCSVDtosExpected = List.of(agencyCSVDto());
        Assertions.assertEquals(agencyCSVDtosExpected, agencyCSVDtos);
    }

    final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toJSONAgencyDto() throws Exception {
        List<AgencyDto> agencyDtos = List.of(agencyDto());
        String json = objectMapper.writeValueAsString(agencyDtos);
        String expectedJson = PropertiesUtils.getResourceAsString("agency/agenciesDto.json");
        System.out.println(json);
        JSONAssert.assertEquals(expectedJson, json, true);
    }

    @Test
    void toJSONAgenciesModel() throws Exception {
        List<AgenciesModel> agenciesModels = List.of(agencyModel());
        String json = objectMapper.writeValueAsString(agenciesModels);
        String expectedJson = PropertiesUtils.getResourceAsString("agency/agenciesModels.json");
        System.out.println(json);
        JSONAssert.assertEquals(expectedJson, json, true);
    }

    @Test
    void toCSVAgencyCSVDto() throws Exception {
        final CsvMapper csvMapper = new CsvMapper();
        final CsvSchema schema = csvMapper.schemaFor(AgencyCSVDto.class).withColumnSeparator(',').withHeader();
        final ObjectWriter writer = csvMapper.writer(schema);
        StringWriter stringWriter = new StringWriter();
        writer.writeValue(stringWriter, agencyCSVDto());

        String expectedCSV = PropertiesUtils.getResourceAsString("agency/AgencyCSVDto.csv");
        Assertions.assertEquals(expectedCSV, stringWriter.toString());
    }

    private AgencyDto agencyDto() {
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setId("agency_id");
        agencyDto.setTenant(4);
        agencyDto.setVersion(12);
        agencyDto.setName("agency_id");
        agencyDto.setIdentifier("agency_identifier");
        agencyDto.setDescription("agency_description");
        agencyDto.setEntityType("agency_entityType");
        agencyDto.setNameEntryParallel(List.of("agency_nameEntryParallel"));
        agencyDto.setAuthorizedForm(List.of("agency_authorizedForm"));
        agencyDto.setAlternativeForm(List.of("agency_alternativeForm"));
        agencyDto.setEntityId("agency_entityId");
        agencyDto.setFromDate("agency_fromDate");
        agencyDto.setToDate("agency_toDate");
        agencyDto.setFunctions(List.of("agency_functions"));
        agencyDto.setBiogHist("agency_biogHist");
        agencyDto.setPlaces(List.of("agency_places"));
        agencyDto.setLegalStatuses(List.of("agency_legalStatuses"));
        agencyDto.setMandates(List.of("agency_mandates"));
        agencyDto.setStructureOrGenealogy("agency_structureOrGenealogy");
        agencyDto.setGeneralContext("agency_generalContext");
        agencyDto.setCreationDate("agency_creationDate");
        agencyDto.setUpdateDate("agency_updateDate");
        agencyDto.setMaintenanceStatus("agency_maintenanceStatus");
        agencyDto.setLocalStatus("agency_localStatus");
        agencyDto.setSources(List.of("agency_sources"));
        agencyDto.setEventDescription("agency_eventDescription");
        return agencyDto;
    }

    private AgenciesModel agencyModel() {
        AgenciesModel agencyModel = new AgenciesModel();
        agencyModel.setId("agency_id");
        agencyModel.setTenant(4);
        agencyModel.setVersion(12);
        agencyModel.setName("agency_id");
        agencyModel.setIdentifier("agency_identifier");
        agencyModel.setDescription("agency_description");
        agencyModel.setEntityType("agency_entityType");
        agencyModel.setNameEntryParallel(List.of("agency_nameEntryParallel"));
        agencyModel.setAuthorizedForm(List.of("agency_authorizedForm"));
        agencyModel.setAlternativeForm(List.of("agency_alternativeForm"));
        agencyModel.setEntityId("agency_entityId");
        agencyModel.setFromDate("agency_fromDate");
        agencyModel.setToDate("agency_toDate");
        agencyModel.setFunctions(List.of("agency_functions"));
        agencyModel.setBiogHist("agency_biogHist");
        agencyModel.setPlaces(List.of("agency_places"));
        agencyModel.setLegalStatuses(List.of("agency_legalStatuses"));
        agencyModel.setMandates(List.of("agency_mandates"));
        agencyModel.setStructureOrGenealogy("agency_structureOrGenealogy");
        agencyModel.setGeneralContext("agency_generalContext");
        agencyModel.setCreationDate("agency_creationDate");
        agencyModel.setUpdateDate("agency_updateDate");
        agencyModel.setMaintenanceStatus("agency_maintenanceStatus");
        agencyModel.setLocalStatus("agency_localStatus");
        agencyModel.setSources(List.of("agency_sources"));
        agencyModel.setEventDescription("agency_eventDescription");
        return agencyModel;
    }

    private AgencyCSVDto agencyCSVDto() {
        AgencyCSVDto agencyCSVDto = new AgencyCSVDto();
        //agencyCSVDto.setId("agency_id");
        //agencyCSVDto.setTenant(4);
        //agencyCSVDto.setVersion(12);
        agencyCSVDto.setName("agency_id");
        agencyCSVDto.setIdentifier("agency_identifier");
        agencyCSVDto.setDescription("agency_description");
        agencyCSVDto.setEntityType("agency_entityType");
        agencyCSVDto.setNameEntryParallel(List.of("agency_nameEntryParallel"));
        agencyCSVDto.setAuthorizedForm(List.of("agency_authorizedForm"));
        agencyCSVDto.setAlternativeForm(List.of("agency_alternativeForm"));
        agencyCSVDto.setEntityId("agency_entityId");
        agencyCSVDto.setFromDate("agency_fromDate");
        agencyCSVDto.setToDate("agency_toDate");
        agencyCSVDto.setFunctions(List.of("agency_functions"));
        agencyCSVDto.setBiogHist("agency_biogHist");
        agencyCSVDto.setPlaces(List.of("agency_places"));
        agencyCSVDto.setLegalStatuses(List.of("agency_legalStatuses"));
        agencyCSVDto.setMandates(List.of("agency_mandates"));
        agencyCSVDto.setStructureOrGenealogy("agency_structureOrGenealogy");
        agencyCSVDto.setGeneralContext("agency_generalContext");
        agencyCSVDto.setMaintenanceStatus("agency_maintenanceStatus");
        agencyCSVDto.setLocalStatus("agency_localStatus");
        agencyCSVDto.setSources(List.of("agency_sources"));
        agencyCSVDto.setEventDescription("agency_eventDescription");
        return agencyCSVDto;
    }
}
