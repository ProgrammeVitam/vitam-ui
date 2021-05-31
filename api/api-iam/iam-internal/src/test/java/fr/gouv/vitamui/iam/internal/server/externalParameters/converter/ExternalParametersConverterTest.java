package fr.gouv.vitamui.iam.internal.server.externalParameters.converter;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.Parameter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ExternalParametersConverterTest {

    private DummyData dummyData;
    final ExternalParametersConverter externalParametersConverter = new ExternalParametersConverter();

    @Before
    public void setUp() {
        dummyData = new DummyData();
    }

    @Test
    public void testConvertEntityToDto() {
        // GIVEN
        ExternalParameters externalParameters = dummyData.externalParameters();

        // WHEN
        ExternalParametersDto dto = externalParametersConverter.convertEntityToDto(externalParameters);

        // THEN
        ExternalParametersDto expected = dummyData.externalParametersDto();
        assertThat(dto).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void testConvertDtoToEntity() {
        // GIVEN
        ExternalParametersDto dto = dummyData.externalParametersDto();

        // WHEN
        ExternalParameters externalParameters = externalParametersConverter.convertDtoToEntity(dto);

        //THEN
        ExternalParameters expected = dummyData.externalParameters();
        assertThat(externalParameters).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        // GIVEN
        ExternalParametersDto dto = dummyData.externalParametersDto();

        // WHEN
        String json = externalParametersConverter.convertToLogbook(dto);

        // THEN
        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertEquals(DummyData.TEST_ID, jsonNode.get(ExternalParametersConverter.ID_KEY).asText());
        assertEquals(DummyData.TEST_IDENTIFIER, jsonNode.get(ExternalParametersConverter.IDENTIFIER_KEY).asText());
        assertEquals(DummyData.TEST_NAME, jsonNode.get(ExternalParametersConverter.NAME_KEY).asText());
        assertThat(jsonNode.get(ExternalParametersConverter.PARAMETERS_KEY)).isNotNull();
    }

    private static class DummyData {

        static final String TEST_ID = "id";
        static final String TEST_IDENTIFIER = "identifier";
        static final String TEST_NAME = "name";
        static final String TEST_KEY = "key";
        static final String TEST_VALUE = "value";

        ExternalParametersDto externalParametersDto() {
            ExternalParametersDto dto = new ExternalParametersDto();
            dto.setId(TEST_ID);
            dto.setIdentifier(TEST_IDENTIFIER);
            dto.setName(TEST_NAME);

            List<ParameterDto> parametersDto = new ArrayList<>();
            ParameterDto parameterDto = new ParameterDto();
            parameterDto.setKey(TEST_KEY);
            parameterDto.setValue(TEST_VALUE);
            parametersDto.add(parameterDto);
            dto.setParameters(parametersDto);
            return dto;
        }

        ExternalParameters externalParameters() {
            ExternalParameters externalParameters = new ExternalParameters();
            externalParameters.setId(TEST_ID);
            externalParameters.setIdentifier(TEST_IDENTIFIER);
            externalParameters.setName(TEST_NAME);

            List<Parameter> parameters = new ArrayList<>();
            Parameter parameter = new Parameter();
            parameter.setKey(TEST_KEY);
            parameter.setValue(TEST_VALUE);
            parameters.add(parameter);
            externalParameters.setParameters(parameters);
            return externalParameters;
        }
    }
}
