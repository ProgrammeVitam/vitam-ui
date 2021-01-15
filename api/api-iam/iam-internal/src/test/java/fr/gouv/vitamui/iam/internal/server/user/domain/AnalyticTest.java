package fr.gouv.vitamui.iam.internal.server.user.domain;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalyticTest {

    private Analytics sut;

    @BeforeEach
    void setUp() {
        sut = new Analytics();
        assertThat(sut.getApplications()).isNullOrEmpty();
    }

    @Test
    void shouldAddApplicationWhenNotExist() {
        String applicationId = "PROFILES_APP";

        sut.tagApplicationAsLastUsed(applicationId);

        assertThat(sut.getApplications()).hasSize(1);
        assertThat(sut.getApplications().get(0).getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getApplications().get(0).getAccessCounter()).isEqualTo(1);
    }

    @Test
    void shouldUpdateApplicationWhenExists() {
        String applicationId = "HIERARCHY_PROFILE_APP";
        sut.tagApplicationAsLastUsed(applicationId);
        assertThat(sut.getApplications()).hasSize(1);
        assertThat(sut.getApplications().get(0).getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getApplications().get(0).getAccessCounter()).isEqualTo(1);

        sut.tagApplicationAsLastUsed(applicationId);
        sut.tagApplicationAsLastUsed(applicationId);
        sut.tagApplicationAsLastUsed(applicationId);

        assertThat(sut.getApplications()).hasSize(1);
        assertThat(sut.getApplications().get(0).getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getApplications().get(0).getAccessCounter()).isEqualTo(4);
    }

    @Test
    void shouldAddTwoApplication() {
        String applicationId = "HIERARCHY_PROFILE_APP";
        sut.tagApplicationAsLastUsed(applicationId);

        String applicationId2 = "PROFILES_APP";
        sut.tagApplicationAsLastUsed(applicationId2);

        assertThat(sut.getApplications()).hasSize(2);
        assertThat(sut.getApplications().get(0).getApplicationId()).isEqualTo(applicationId);
        assertThat(sut.getApplications().get(0).getAccessCounter()).isEqualTo(1);
        assertThat(sut.getApplications().get(1).getApplicationId()).isEqualTo(applicationId2);
        assertThat(sut.getApplications().get(1).getAccessCounter()).isEqualTo(1);
    }

    @Test
    void shouldAddLastTenantIdentifier() {
        sut.setLastTenantIdentifier(10);
        assertThat(sut.getLastTenantIdentifier()).isNotNull();
        assertThat(sut.getLastTenantIdentifier()).isEqualTo(10);
    }
}
