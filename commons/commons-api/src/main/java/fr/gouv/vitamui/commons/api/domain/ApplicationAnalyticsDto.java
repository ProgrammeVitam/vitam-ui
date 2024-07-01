package fr.gouv.vitamui.commons.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString(callSuper = true)
public class ApplicationAnalyticsDto implements Serializable {

    private String applicationId;

    private int accessCounter;

    private OffsetDateTime lastAccess;
}
