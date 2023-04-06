package fr.gouv.vitamui.iam.internal.server.user.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class AlertAnalytics implements Serializable {
    private String applicationId;

    private String creationDate;

    private String id;

    private String status;

    private String identifier;

    private String type;

    private String key;

    private String action;

}
