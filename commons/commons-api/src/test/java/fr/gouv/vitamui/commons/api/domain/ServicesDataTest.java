package fr.gouv.vitamui.commons.api.domain;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ServicesDataTest {

    @Test
    public void getServiceByNameTest() {
        List<String> services = ServicesData.getServicesByName(ServicesData.SERVICE_USERS);
        assertThat(services).isNotEmpty();
        assertThat(services).size().isEqualTo(10);

        services = ServicesData.getServicesByName(ServicesData.SERVICE_USERS, ServicesData.SERVICE_PROVIDERS);
        assertThat(services).isNotEmpty();
    }
}
