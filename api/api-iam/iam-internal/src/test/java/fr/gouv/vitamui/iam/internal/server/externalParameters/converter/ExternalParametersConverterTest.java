package fr.gouv.vitamui.iam.internal.server.externalParameters.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.Parameter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;

public class ExternalParametersConverterTest {

    private final ExternalParametersConverter externalParametersConverter = new ExternalParametersConverter();
    
    private static final String TEST_ID = "id";
    private static final String TEST_IDENTIFIER = "identifier";
    private static final String TEST_NAME = "name";
    private static final String TEST_KEY = "key";
    private static final String TEST_VALUE = "value";

    @Test
    public void testConvertEntityToDto() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setId(TEST_ID);
        externalParameters.setIdentifier(TEST_IDENTIFIER);
        externalParameters.setName(TEST_NAME);
        
        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = new Parameter();
        parameter.setKey(TEST_KEY);
        parameter.setValue(TEST_VALUE);
        parameters.add(parameter);
        externalParameters.setParameters(parameters);

        ExternalParametersDto dto = externalParametersConverter.convertEntityToDto(externalParameters);
        assertThat(dto).isEqualToIgnoringGivenFields(externalParameters);
    }

    @Test
    public void testConvertDtoToEntity() {
        ExternalParametersDto dto = new ExternalParametersDto();
        dto.setId(TEST_ID);
        dto.setIdentifier(TEST_IDENTIFIER);
        dto.setName(TEST_NAME);
        
        List<ParameterDto> parametersDto = new ArrayList<ParameterDto>();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey(TEST_KEY);
        parameterDto.setValue(TEST_VALUE);
        parametersDto.add(parameterDto);
        dto.setParameters(parametersDto);

        ExternalParameters res = externalParametersConverter.convertDtoToEntity(dto);
        assertThat(dto).isEqualToIgnoringGivenFields(res);
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        ExternalParametersDto dto = new ExternalParametersDto();
        dto.setId(TEST_ID);
        dto.setIdentifier(TEST_IDENTIFIER);
        dto.setName(TEST_NAME);
        
        List<ParameterDto> parametersDto = new ArrayList<ParameterDto>();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey(TEST_KEY);
        parameterDto.setValue(TEST_VALUE);
        parametersDto.add(parameterDto);
        dto.setParameters(parametersDto);

        String json = externalParametersConverter.convertToLogbook(dto);

        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        
        assertEquals(jsonNode.get(ExternalParametersConverter.ID_KEY).asText(), TEST_ID);
        assertEquals(jsonNode.get(ExternalParametersConverter.IDENTIFIER_KEY).asText(), TEST_IDENTIFIER);
        assertEquals(jsonNode.get(ExternalParametersConverter.NAME_KEY).asText(), TEST_NAME);
        assertThat(jsonNode.get(ExternalParametersConverter.PARAMETERS_KEY)).isNotNull();      
    }
}
