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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;

public class Jackson2GenericHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    private final ObjectMapper jackson2Mapper;

    public Jackson2GenericHttpMessageConverter(final ObjectMapper jackson2Mapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
        this.jackson2Mapper = jackson2Mapper;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return true;
    }

    @Override
    public Object read(final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        final JavaType javaType = jackson2Mapper.getTypeFactory().constructType(type);
        return jackson2Mapper.readValue(inputMessage.getBody(), javaType);
    }

    @Override
    protected Object readInternal(final Class<?> clazz, final HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        return jackson2Mapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(final Object value, final Type type, final HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        jackson2Mapper.writeValue(outputMessage.getBody(), value);
    }
}
