package fr.gouv.vitamui.commons.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

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
