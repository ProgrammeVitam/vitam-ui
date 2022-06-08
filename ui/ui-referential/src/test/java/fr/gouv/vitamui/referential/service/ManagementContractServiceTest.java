package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.external.client.ManagementContractExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class ManagementContractServiceTest {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ManagementContractService.class);

    @Mock
    private ManagementContractExternalRestClient client;

    @Mock
    private CommonService commonService;
    private ManagementContractService service;

    @Before
    public void setUp() {
        service = new ManagementContractService(commonService, client);
    }

    @Test
    public void testGetAll() {
        List<ManagementContractDto> managementContracts = new ArrayList<>();
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);
        managementContracts.add(mcd);

        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(managementContracts);

        final Collection<ManagementContractDto> mcdList = service.getAll(null, Optional.empty());
        Assert.assertNotNull(managementContracts);
        assertThat(mcdList).containsExactly(mcd);
    }

    @Test
    public void testCheck() {
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);

        Mockito.when(client.check(isNull(), any(ManagementContractDto.class))).thenReturn(true);
        final boolean check = service.check(null, mcd);
        assertThat(check).isEqualTo(true);
    }

    @Test
    public void create() {
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setTenant(0);

        Mockito.when(client.create(isNull(), any(ManagementContractDto.class))).thenReturn(mcd);
        final ManagementContractDto mcdResult = service.create(null, mcd);
        Assert.assertNotNull(mcdResult);
        Assert.assertEquals(Integer.valueOf(0), mcdResult.getTenant());
    }
}
