package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ApplicationAnalyticsDto implements Serializable {

    private String applicationId;

    private int accessCounter;

    private OffsetDateTime lastAccess;
}
