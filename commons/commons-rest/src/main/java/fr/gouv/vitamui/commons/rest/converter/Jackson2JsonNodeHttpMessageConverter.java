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
import java.lang.reflect.Field;

/**
 * Allows Jackson 2 / Jackson 3 coexistence for {@code @RequestBody com.fasterxml.jackson.databind.JsonNode}
 * parameters and DTOs containing a Jackson 2 {@link JsonNode}. Spring Boot 4 registers a Jackson 3
 * ({@code tools.jackson}) converter by default, which cannot instantiate the abstract Jackson 2 type.
 * <p>
 * The converter deliberately declines unrelated types so that they remain handled by the default Jackson 3 converter.
 * <p>
 * To remove once the controllers, their services and the Vitam SDK are migrated to Jackson 3.
 */
public class Jackson2JsonNodeHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper jackson2Mapper;

    public Jackson2JsonNodeHttpMessageConverter(final ObjectMapper jackson2Mapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.jackson2Mapper = jackson2Mapper;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return requiresJackson2(clazz);
    }

    @Override
    protected Object readInternal(final Class<?> clazz, final HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        return jackson2Mapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(final Object value, final HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        jackson2Mapper.writeValue(outputMessage.getBody(), value);
    }

    @Override
    public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
        return JsonNode.class.isAssignableFrom(clazz) && super.canWrite(clazz, mediaType);
    }

    private static boolean requiresJackson2(final Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (JsonNode.class.isAssignableFrom(clazz)) {
            return true;
        }

        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (JsonNode.class.isAssignableFrom(field.getType())) {
                    return true;
                }
            }
        }
        return false;
    }
}
