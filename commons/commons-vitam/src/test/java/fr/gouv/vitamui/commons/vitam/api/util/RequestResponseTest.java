package fr.gouv.vitamui.commons.vitam.api.util;

import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import java.util.List;

public class RequestResponseTest {
    @Test
    public void testDeserialization() throws Exception {
        RequestResponseOK<com.fasterxml.jackson.databind.JsonNode> ok = new RequestResponseOK<>();
        ok.setHttpCode(200);
        String json = fr.gouv.vitam.common.json.JsonHandler.writeAsString(ok);
        RequestResponse<JsonNode> res = (RequestResponse<JsonNode>) JsonUtils.fromJson(json, RequestResponse.class);
        System.out.println("Result: " + res.getHttpCode());
    }
}
