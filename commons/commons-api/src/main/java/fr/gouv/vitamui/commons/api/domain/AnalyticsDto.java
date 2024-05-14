package fr.gouv.vitamui.commons.api.domain;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
public class AnalyticsDto implements Serializable {

    private List<ApplicationAnalyticsDto> applications = new ArrayList<>();

    private Integer lastTenantIdentifier;

    private List<AlertAnalyticsDto> alerts = new ArrayList<>();
}
