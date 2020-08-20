package fr.gouv.vitamui.iam.internal.server.user.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Data;

@Data
public class Analytics implements Serializable {

    private List<ApplicationAnalytics> applications = new ArrayList<>();

    private Integer lastTenantIdentifier;

    public void tagApplicationAsLastUsed(String applicationId) {
        applications.stream().filter(application -> Objects.equals(application.getApplicationId(), applicationId)).findAny()
                .ifPresentOrElse(ApplicationAnalytics::tagAsLastUsed, () -> applications.add(new ApplicationAnalytics(applicationId)));
    }

}
