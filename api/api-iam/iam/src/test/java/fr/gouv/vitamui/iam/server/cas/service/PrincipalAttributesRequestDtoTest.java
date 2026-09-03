package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The request the authentication server sends may omit {@code apiContext} (a primitive boolean). With
 * the same Jackson the IAM controller uses, deserialization must default it to false instead of failing.
 */
class PrincipalAttributesRequestDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void deserializesWithoutApiContextAsFalse() {
        final PrincipalAttributesRequestDto dto = mapper.readValue(
            "{\"loginEmail\":\"user@test.fr\",\"loginCustomerId\":\"customerA\"}",
            PrincipalAttributesRequestDto.class
        );

        assertThat(dto.getLoginEmail()).isEqualTo("user@test.fr");
        assertThat(dto.getLoginCustomerId()).isEqualTo("customerA");
        assertThat(dto.isApiContext()).isFalse();
    }

    @Test
    void deserializesWithApiContextTrue() {
        final PrincipalAttributesRequestDto dto = mapper.readValue(
            "{\"loginEmail\":\"user@test.fr\",\"loginCustomerId\":\"customerA\",\"apiContext\":true}",
            PrincipalAttributesRequestDto.class
        );

        assertThat(dto.isApiContext()).isTrue();
    }
}
