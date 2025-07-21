package fr.gouv.vitamui.referential.server.config;

import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Accessors(chain = true)
@Validated
public class ExternalArchivingSystemClientConfig {

    @NotBlank
    private String archivingSystemId;

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private RestClientConfiguration accessExternalClient;
}
