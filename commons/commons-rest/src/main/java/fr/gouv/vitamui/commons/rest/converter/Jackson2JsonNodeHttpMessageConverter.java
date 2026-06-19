/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.commons.rest.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Allows Jackson 2 / Jackson 3 coexistence for {@code @RequestBody com.fasterxml.jackson.databind.JsonNode}
 * parameters. Spring Boot 4 registers a Jackson 3 ({@code tools.jackson}) converter by default, which cannot
 * instantiate the abstract Jackson 2 {@link JsonNode} type. This converter reads the raw request body and parses
 * it explicitly with a Jackson 2 {@link ObjectMapper}.
 * <p>
 * Only handles reading: {@link #canWrite(MediaType)} always returns {@code false} so response serialization is
 * left to the default (Jackson 3) converter.
 * <p>
 * To remove once the controllers, their services and the Vitam SDK are migrated to Jackson 3.
 */
public class Jackson2JsonNodeHttpMessageConverter extends AbstractHttpMessageConverter<JsonNode> {

    private final ObjectMapper jackson2Mapper;

    public Jackson2JsonNodeHttpMessageConverter(final ObjectMapper jackson2Mapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.jackson2Mapper = jackson2Mapper;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonNode readInternal(final Class<? extends JsonNode> clazz, final HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        final String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
        return jackson2Mapper.readTree(body);
    }

    @Override
    protected void writeInternal(final JsonNode jsonNode, final HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write(jackson2Mapper.writeValueAsBytes(jsonNode));
    }

    @Override
    protected boolean canWrite(final MediaType mediaType) {
        return super.canWrite(mediaType);
    }
}
