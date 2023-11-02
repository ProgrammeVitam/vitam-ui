package fr.gouv.vitamui.commons.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString(callSuper = true)
public class AlertAnalyticsDto implements Serializable {
    private String applicationId;

    private String creationDate;

    private String id;

    private String status;

    private String identifier;

    private String type;

    private String key;

    private String action;

}
