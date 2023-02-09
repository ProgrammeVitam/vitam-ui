/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.external.client.ManagementContractExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;
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
