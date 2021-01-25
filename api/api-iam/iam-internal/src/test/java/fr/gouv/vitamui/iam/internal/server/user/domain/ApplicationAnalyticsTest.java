package fr.gouv.vitamui.iam.internal.server.user.domain;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationAnalyticsTest {

    private ApplicationAnalytics sut;

    @Test
    void shouldCreateApplicationWithInitialValues() {
        String applicationId = "PROFILES_APP";

        sut = new ApplicationAnalytics(applicationId);

        assertThat(sut.getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getAccessCounter()).isEqualTo(1);
    }


    @Test
    void shouldIncrementAccessCounter() {
        String applicationId = "GROUPS_APP";
        sut = new ApplicationAnalytics(applicationId);

        sut.tagAsLastUsed();

        assertThat(sut.getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getAccessCounter()).isEqualTo(2);
    }

}
