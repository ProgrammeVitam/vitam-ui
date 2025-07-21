package fr.gouv.vitamui.referential.server.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@Validated
public class ExternalArchivingSystemReferenceConfig {

    @NotBlank
    private String archivingSystemId;

    @NotNull
    @NotEmpty
    private Set<Integer> tenantIds;
}
