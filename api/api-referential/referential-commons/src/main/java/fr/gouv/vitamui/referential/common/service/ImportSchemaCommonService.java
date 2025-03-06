package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.schema.SchemaInputModel;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ImportSchemaCommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportSchemaCommonService.class);

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public ImportSchemaCommonService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse<Void> importUnitSchema(
        final VitamContext vitamContext,
        final List<SchemaInputModel> importSchema
    ) throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeImportSchema(importSchema)) {
            LOGGER.info("Import Unit Schema : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<Void> jsonResponse = adminExternalClient.importUnitExternalSchema(
                vitamContext,
                byteArrayInputStream
            );
            VitamRestUtils.checkResponse(jsonResponse);
            return jsonResponse;
        }
    }

    ByteArrayInputStream serializeImportSchema(final List<SchemaInputModel> schemaInputModel) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.convertValue(schemaInputModel, JsonNode.class);

        LOGGER.debug("The json for import schema, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            objectMapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }
}
