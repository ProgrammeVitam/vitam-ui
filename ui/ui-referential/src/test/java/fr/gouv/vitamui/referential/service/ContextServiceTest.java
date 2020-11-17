package fr.gouv.vitamui.referential.service;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.external.client.ContextExternalRestClient;
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
public class ContextServiceTest {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ContextService.class);
    @Mock
    private ContextExternalRestClient client;
    @Mock
    private CommonService commonService;
    private ContextService service;

    @Before
    public void setUp() {
        service = new ContextService(commonService, client);
    }

    @Test
    public void testGetAll() {
        final List<ContextDto> contextDtos = new ArrayList<ContextDto>();
        ContextDto contextDto = new ContextDto();
        contextDto.setId("id");
        contextDtos.add(contextDto);

        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(contextDtos);

        final Collection<ContextDto> contextList = service.getAll(null, Optional.empty());
        Assert.assertNotNull(contextList);
        assertThat(contextList).containsExactly(contextDto);
    }

    @Test
    public void testCheck() {
        ContextDto contextDto = new ContextDto();
        contextDto.setId("id");

        Mockito.when(client.check(isNull(), any(ContextDto.class))).thenReturn(true);

        final boolean check = service.check(null, contextDto);
        assertThat(check).isEqualTo(true);
    }
}
