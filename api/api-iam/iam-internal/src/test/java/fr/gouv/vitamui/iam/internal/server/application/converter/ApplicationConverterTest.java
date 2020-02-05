package fr.gouv.vitamui.iam.internal.server.application.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;

public class ApplicationConverterTest {

    private final ApplicationConverter applicationConverter = new ApplicationConverter();

    @Test
    public void testConvertEntityToDto() {
        Application app = new Application();
        app.setUrl("url");
        app.setId("identifier");

        ApplicationDto res = applicationConverter.convertEntityToDto(app);
        assertThat(app).isEqualToIgnoringGivenFields(res);
    }

    @Test
    public void testConvertDtoToEntity() {
        ApplicationDto appDto = new ApplicationDto();
        appDto.setUrl("url");
        appDto.setIdentifier("identifier");

        Application res = applicationConverter.convertDtoToEntity(appDto);
        assertThat(appDto).isEqualToIgnoringGivenFields(res);
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setUrl("url");
        applicationDto.setIdentifier("identifier");

        String json = applicationConverter.convertToLogbook(applicationDto);

        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(ApplicationConverter.APPLICATION_ID_KEY)).isNotNull();
        assertThat(jsonNode.get(ApplicationConverter.URL_KEY)).isNotNull();

    }
}
