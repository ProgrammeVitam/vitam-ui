package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La requête envoyée par le serveur d'authentification peut omettre {@code apiContext} (un booléen primitif). Avec
 * le même Jackson que celui utilisé par le contrôleur IAM, la désérialisation doit le mettre à false par défaut au lieu d'échouer.
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
