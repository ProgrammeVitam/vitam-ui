package fr.gouv.vitamui.referential.server.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@Validated
public class ExternalArchivingSystemTenantConfig {

    @NotNull
    private Integer tenant;

    @Valid
    @NotNull
    @NotEmpty
    private List<ExternalArchivingSystemReferenceConfig> externalArchivingSystemReferences = new ArrayList<>();
}
