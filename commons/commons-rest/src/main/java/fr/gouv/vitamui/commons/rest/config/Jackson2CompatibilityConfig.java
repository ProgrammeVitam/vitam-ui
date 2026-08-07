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
package fr.gouv.vitamui.commons.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.rest.converter.Jackson2GenericHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Keeps Jackson 2 working under Spring Boot 4, whose default JSON converter is now Jackson 3
 * ({@code tools.jackson}) and cannot instantiate Jackson 2 types
 * ({@code com.fasterxml.jackson.databind.JsonNode} and the DTOs that contain one, such as the Vitam client).
 *

 * <p>ALL inbound/outbound JSON now goes through Jackson 2. The injected
 * {@code ObjectMapper} bean must therefore be configured completely and consistently (same modules and
 * features as the Jackson 3 layer expected). The Jackson 3 converter stays in the list as a fallback but is
 * no longer selected for {@code application/json}.
 *
 * <p>To be wired with {@code @Import} in each microservice (this class lives outside the base package
 * scanned by {@code @SpringBootApplication}), and to be removed once the whole stack (controllers, services,
 * Vitam client) has migrated to Jackson 3.
 */
@Configuration
public class Jackson2CompatibilityConfig implements WebMvcConfigurer {

    private final ObjectMapper jackson2Mapper;

    public Jackson2CompatibilityConfig(final ObjectMapper jackson2Mapper) {
        this.jackson2Mapper = jackson2Mapper;
    }

    @Override
    public void configureMessageConverters(final HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConvertersList(converters -> insertBeforeJackson3(converters, jackson2Mapper));
    }

    /**
     * Inserts the Jackson 2 converter <b>right before</b> the Jackson 3 converter
     */
    static void insertBeforeJackson3(
        final List<HttpMessageConverter<?>> converters,
        final ObjectMapper jackson2Mapper
    ) {
        final Jackson2GenericHttpMessageConverter jackson2 = new Jackson2GenericHttpMessageConverter(jackson2Mapper);
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof JacksonJsonHttpMessageConverter) {
                converters.add(i, jackson2);
                return;
            }
        }
        converters.add(jackson2);
    }
}
