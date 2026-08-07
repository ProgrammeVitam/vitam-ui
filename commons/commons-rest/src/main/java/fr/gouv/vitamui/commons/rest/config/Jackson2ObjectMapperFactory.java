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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Builds the Jackson 2 {@link ObjectMapper} bean each microservice exposes.
 *
 * <p>Since {@link Jackson2CompatibilityConfig} routes <b>all</b> {@code application/json} bodies through that
 * bean (and no longer only {@code JsonNode} ones), the bean has become the wire contract of every API. It is
 * therefore configured to be <b>byte-for-byte equivalent to the mapper Spring Boot 3 auto-configured</b>,
 * which is what the DTOs were written and tested against. Every line below mirrors one of:
 *
 * <ul>
 *   <li>{@code Jackson2ObjectMapperBuilder#registerWellKnownModulesIfAvailable} — {@code Jdk8Module},
 *       {@code JavaTimeModule}, {@code ParameterNamesModule} (Kotlin and XML are not on our classpath);</li>
 *   <li>{@code Jackson2ObjectMapperBuilder}'s own feature defaults — {@code DEFAULT_VIEW_INCLUSION} and
 *       {@code FAIL_ON_UNKNOWN_PROPERTIES} disabled;</li>
 *   <li>{@code JacksonAutoConfiguration#FEATURE_DEFAULTS} — {@code WRITE_DATES_AS_TIMESTAMPS} and
 *       {@code WRITE_DURATIONS_AS_TIMESTAMPS} disabled;</li>
 *   <li>{@code JacksonAutoConfiguration#parameterNamesModule} — {@code JsonCreator.Mode.DEFAULT}.</li>
 * </ul>
 *
 * <p>Two of those modules are what Jackson 3 has folded into its core, and their absence is what broke in
 * production: {@code JavaTimeModule} (any DTO with an {@code OffsetDateTime} — {@code UserDto#lastConnection},
 * everything implementing {@code IOperationDto} — failed with {@code Java 8 date/time type not supported by
 * default}) and {@code ParameterNamesModule} (DTOs with only an all-args constructor and no
 * {@code @JsonCreator}, such as {@code TermsFacet} inside {@code SearchCriteriaDto#facets}, failed with
 * {@code no Creators, like default constructor, exist} and a 400).
 *
 * <p>Modules are registered explicitly rather than through {@code findAndRegisterModules()}, which would also
 * pick up {@code AfterburnerModule} from the classpath (deprecated in Jackson 2.x, bytecode-generating) on
 * the whole web layer — Spring Boot never did that either.
 *
 * <p>{@code Jackson2ObjectMapperFactoryTest} pins this contract; keep the two in sync.
 *
 * <p>To be removed once the whole stack (controllers, services, Vitam client) has migrated to Jackson 3.
 * Note that Jackson 3 is <i>not</i> bug-for-bug identical to this mapper (it flips several defaults), so that
 * migration is a behavioural change to plan, not a drop-in swap.
 */
public final class Jackson2ObjectMapperFactory {

    private Jackson2ObjectMapperFactory() {
        // utility class
    }

    public static ObjectMapper create() {
        return JsonMapper.builder()
            // Well-known modules, as registered by Jackson2ObjectMapperBuilder#registerWellKnownModulesIfAvailable.
            .addModule(new JavaTimeModule())
            .addModule(new Jdk8Module())
            // JsonCreator.Mode.DEFAULT, exactly as JacksonAutoConfiguration#parameterNamesModule declares it:
            // it leaves the single-argument delegating-vs-properties arbitration to Jackson's own heuristics
            // rather than forcing one mode.
            .addModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT))
            // Jackson2ObjectMapperBuilder defaults.
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // JacksonAutoConfiguration#FEATURE_DEFAULTS.
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .build();
    }
}
