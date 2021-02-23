package fr.gouv.vitamui.iam.internal.server.externalParameters.converter;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testConvertEntityToDto() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setId("id");
        externalParameters.setIdentifier("identifier");
        externalParameters.setName("name");

        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = new Parameter();
        parameter.setKey("key");
        parameter.setValue("value");
        parameters.add(parameter);
        externalParameters.setParameters(parameters);

        ExternalParametersDto dto = externalParametersConverter.convertEntityToDto(externalParameters);
        assertThat(externalParameters).isEqualToIgnoringGivenFields(dto, "customerId");
    }

    @Test
    public void testConvertDtoToEntity() {
        ExternalParametersDto dto = new ExternalParametersDto();
        dto.setId("id");
        dto.setIdentifier("identifier");
        dto.setName("name");

        List<ParameterDto> parametersDto = new ArrayList<ParameterDto>();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey("key");
        parameterDto.setValue("value");
        parametersDto.add(parameterDto);
        dto.setParameters(parametersDto);

        ExternalParameters res = externalParametersConverter.convertDtoToEntity(dto);
        assertThat(dto).isEqualToIgnoringGivenFields(res);
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        ExternalParametersDto dto = new ExternalParametersDto();
        dto.setId("id");
        dto.setIdentifier("identifier");
        dto.setName("name");

        List<ParameterDto> parametersDto = new ArrayList<ParameterDto>();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setKey("key");
        parameterDto.setValue("value");
        parametersDto.add(parameterDto);
        dto.setParameters(parametersDto);

        String json = externalParametersConverter.convertToLogbook(dto);

        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);

        assertThat(jsonNode.get(ExternalParametersConverter.ID_KEY)).isNotNull();
        assertThat(jsonNode.get(ExternalParametersConverter.IDENTIFIER_KEY)).isNotNull();
        assertThat(jsonNode.get(ExternalParametersConverter.NAME_KEY)).isNotNull();
        assertThat(jsonNode.get(ExternalParametersConverter.PARAMETERS_KEY)).isNotNull();
    }
}
