package fr.gouv.vitamui.commons.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class AnalyticsDto implements Serializable {

    private List<ApplicationAnalyticsDto> applications = new ArrayList<>();

    private Integer lastTenantIdentifier;
}
