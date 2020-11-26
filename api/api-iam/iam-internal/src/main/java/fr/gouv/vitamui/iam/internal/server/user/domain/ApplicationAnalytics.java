package fr.gouv.vitamui.iam.internal.server.user.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class ApplicationAnalytics implements Serializable {

    private static final int INIT_COUNTER = 1;

    private String applicationId;

    private int accessCounter;

    private OffsetDateTime lastAccess;


    public ApplicationAnalytics(String applicationId) {
        this.setApplicationId(applicationId);
        this.setAccessCounter(INIT_COUNTER);
        this.setLastAccess(OffsetDateTime.now());
    }

    public void tagAsLastUsed() {
        this.lastAccess = OffsetDateTime.now();
        this.accessCounter++;
    }
}
