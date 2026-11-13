package fr.gouv.vitamui.commons.api.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ServicesDataTest {

    @Test
    void getServiceByNameTest() {
        List<String> services = ServicesData.getServicesByName(ServicesData.SERVICE_USERS);
        assertThat(services).isNotEmpty();
        assertThat(services).size().isEqualTo(10);

        services = ServicesData.getServicesByName(ServicesData.SERVICE_USERS, ServicesData.SERVICE_PROVIDERS);
        assertThat(services).isNotEmpty();
    }

    @Test
    void archiveProfileRolesShouldMatchTheirAction() {
        assertThat(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES).isEqualTo("ROLE_UPDATE_ARCHIVE_PROFILES");
        assertThat(ServicesData.ROLE_CREATE_ARCHIVE_PROFILES).isEqualTo("ROLE_CREATE_ARCHIVE_PROFILES");
        assertThat(ServicesData.ROLE_IMPORT_ARCHIVE_PROFILES).isEqualTo("ROLE_IMPORT_ARCHIVE_PROFILES");
        assertThat(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES_UNIT).isEqualTo("ROLE_UPDATE_ARCHIVE_PROFILES_UNIT");
        assertThat(ServicesData.ROLE_CREATE_ARCHIVE_PROFILES_UNIT).isEqualTo("ROLE_CREATE_ARCHIVE_PROFILES_UNIT");
        assertThat(ServicesData.ROLE_IMPORT_ARCHIVE_PROFILES_UNIT).isEqualTo("ROLE_IMPORT_ARCHIVE_PROFILES_UNIT");
    }
}
