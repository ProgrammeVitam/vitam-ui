package fr.gouv.vitamui.commons.test.utils;

import fr.gouv.vitamui.commons.api.domain.AnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.ApplicationAnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;

import java.time.OffsetDateTime;
import java.util.List;

public class UserBuilder {


    public static UserDto buildWithAnalytics() {
        ApplicationAnalyticsDto applicationAnalytic = new ApplicationAnalyticsDto();
        applicationAnalytic.setAccessCounter(9546);
        applicationAnalytic.setLastAccess(OffsetDateTime.now());
        applicationAnalytic.setApplicationId("INGEST_SUPERVISION_APP");

        AnalyticsDto analytics = new AnalyticsDto();
        analytics.setApplications(List.of(applicationAnalytic));

        UserDto user = new UserDto();
        user.setId("78");
        user.setEmail("test@user.fr");
        user.setAnalytics(analytics);

        return user;
    }
}
