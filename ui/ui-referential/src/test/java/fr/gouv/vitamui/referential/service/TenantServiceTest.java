package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.iam.external.client.TenantExternalRestClient;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class TenantServiceTest {

    @Mock
    private TenantExternalRestClient client;

    private TenantService service;

    @Before
    public void setUp() {
        service = new TenantService(client);
    }

    @Test
    public void testGetAll() {
        final List<TenantDto> tenantDtos = new ArrayList<TenantDto>();
        TenantDto tenantDto = new TenantDto();
        tenantDto.setId("id");
        tenantDtos.add(tenantDto);

        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(tenantDtos);

        final Collection<TenantDto> tenantList = service.getAll(null, Optional.empty());
        Assert.assertNotNull(tenantList);
        assertThat(tenantList).containsExactly(tenantDto);
    }

}
