package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AnalyticsDto implements Serializable {
    private List<ApplicationAnalyticsDto> applications;

    private Integer lastTenantIdentifier;

}
