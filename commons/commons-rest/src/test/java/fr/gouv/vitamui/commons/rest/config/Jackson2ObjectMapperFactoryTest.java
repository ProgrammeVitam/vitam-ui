package fr.gouv.vitamui.commons.rest.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the modules {@link Jackson2ObjectMapperFactory} must register for the Jackson 2 mapper to be a
 * drop-in replacement for the Jackson 3 one, which has both built in.
 *
 * <p>Both cases below produced a runtime failure in production when the module was missing:
 * an {@code InvalidDefinitionException} on every request carrying the type.
 */
class Jackson2ObjectMapperFactoryTest {

    private final ObjectMapper mapper = Jackson2ObjectMapperFactory.create();

    /** DTO shaped like {@code TermsFacet}: all-args constructor only, no @JsonCreator, no no-arg ctor. */
    static class AllArgsCtorOnlyDto {

        private String name;
        private Integer size;

        AllArgsCtorOnlyDto(final String name, final Integer size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(final Integer size) {
            this.size = size;
        }
    }

    /** DTO carrying a java.time type, like {@code UserDto#lastConnection}. */
    static class DatedDto {

        private OffsetDateTime date;

        public OffsetDateTime getDate() {
            return date;
        }

        public void setDate(final OffsetDateTime date) {
            this.date = date;
        }
    }

    @Test
    void whenTypeHasOnlyAnAllArgsConstructor_thenParameterNamesModuleMakesItDeserializable() throws Exception {
        final AllArgsCtorOnlyDto dto = mapper.readValue("{\"name\":\"myFacet\",\"size\":10}", AllArgsCtorOnlyDto.class);

        assertThat(dto.getName()).isEqualTo("myFacet");
        assertThat(dto.getSize()).isEqualTo(10);
    }

    @Test
    void whenTypeCarriesAnOffsetDateTime_thenJavaTimeModuleHandlesItBothWays() throws Exception {
        final DatedDto dto = mapper.readValue("{\"date\":\"2026-07-21T10:15:30+02:00\"}", DatedDto.class);

        // Same instant. Jackson normalizes to UTC on read (ADJUST_DATES_TO_CONTEXT_TIME_ZONE, on by default),
        // so the offset is not preserved — only the instant is.
        assertThat(dto.getDate().toInstant()).isEqualTo(OffsetDateTime.parse("2026-07-21T10:15:30+02:00").toInstant());

        // ISO-8601 string, not a numeric timestamp: WRITE_DATES_AS_TIMESTAMPS is disabled.
        assertThat(mapper.writeValueAsString(dto)).isEqualTo("{\"date\":\"2026-07-21T08:15:30Z\"}");
    }

    @Test
    void whenBodyCarriesAnUnknownField_thenItIsIgnored() throws Exception {
        assertThat(mapper.readValue("{\"date\":null,\"unknown\":\"x\"}", DatedDto.class)).isNotNull();
    }

    /** DTO carrying the two types the remaining well-known modules cover. */
    static class Jdk8AndDurationDto {

        private Optional<String> maybe = Optional.empty();
        private Duration duration;

        public Optional<String> getMaybe() {
            return maybe;
        }

        public void setMaybe(final Optional<String> maybe) {
            this.maybe = maybe;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(final Duration duration) {
            this.duration = duration;
        }
    }

    @Test
    void whenTypeCarriesOptionalAndDuration_thenTheyUseTheirBoot3Representation() throws Exception {
        final Jdk8AndDurationDto dto = new Jdk8AndDurationDto();
        dto.setMaybe(Optional.of("here"));
        dto.setDuration(Duration.ofSeconds(90));

        // Jdk8Module unwraps Optional instead of exposing {"present":true};
        // WRITE_DURATIONS_AS_TIMESTAMPS disabled gives ISO-8601, not 90.0.
        assertThat(mapper.writeValueAsString(dto)).isEqualTo("{\"maybe\":\"here\",\"duration\":\"PT1M30S\"}");
    }

    /**
     * The mapper is meant to be indistinguishable from the one Spring Boot 3 auto-configured, since that is
     * what every DTO on the wire was written against. Sources, verified against the 3.5.x bytecode:
     * {@code Jackson2ObjectMapperBuilder} for the first two, {@code JacksonAutoConfiguration#FEATURE_DEFAULTS}
     * for the last two.
     */
    @Test
    void thenFeatureDefaultsMatchSpringBoot3() {
        assertThat(mapper.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION)).isFalse();
        assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)).isFalse();
    }

    /** Same set as {@code Jackson2ObjectMapperBuilder#registerWellKnownModulesIfAvailable} minus Kotlin/XML. */
    @Test
    void thenRegisteredModulesMatchSpringBoot3() {
        assertThat(mapper.getRegisteredModuleIds()).containsExactlyInAnyOrder(
            "jackson-datatype-jsr310",
            "com.fasterxml.jackson.datatype.jdk8.Jdk8Module",
            "jackson-module-parameter-names"
        );
    }
}
