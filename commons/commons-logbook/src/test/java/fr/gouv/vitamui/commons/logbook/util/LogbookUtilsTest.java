package fr.gouv.vitamui.commons.logbook.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;

public class LogbookUtilsTest {

    @Test
    public void testRetrieveJsonData() throws InvalidParseOperationException {
        EventDiffDto evData = new EventDiffDto("Identifiant du contrat d'accès pour l'arbre", "AC-00001", "AC-00002");
        String evDetDataString = LogbookUtils.getEvData(Arrays.asList(evData)).toString();
        JsonNode evDetData = JsonHandler.getFromString(evDetDataString);
        assertThat(evDetData).isNotNull();

    }
}
