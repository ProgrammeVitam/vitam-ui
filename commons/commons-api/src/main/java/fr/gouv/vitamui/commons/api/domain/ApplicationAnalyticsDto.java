package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class ApplicationAnalyticsDto implements Serializable {

    private String applicationId;

    private int accessCounter;

    private OffsetDateTime lastAccess;
}
