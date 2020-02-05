package fr.gouv.vitamui.ui.commons.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import fr.gouv.vitamui.commons.rest.client.logbook.LogbookExternalRestClient;

/**
 * Unit test for {@link LogbookService}.
 *
 *
 */
@RunWith(SpringRunner.class)
public class LogbookServiceTest {

    private LogbookService logbookService;

    @Mock
    private LogbookExternalRestClient baseLogbookRestClient;

    @Before
    public void init() {
        logbookService = new LogbookService(baseLogbookRestClient);
    }

    /**
     * Test method for {@link LogbookService#findUnitLifeCyclesByUnitId}.
     */
    @Test
    public void testFindUnitLifeCyclesByUnitId() {
        when(baseLogbookRestClient.findUnitLifeCyclesByUnitId(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(JsonNodeFactory.instance.objectNode());
        assertNotNull(logbookService.findUnitLifeCyclesByUnitId(null, null));
    }

    /**
     * Test method for {@link LogbookService#findObjectLifeCyclesByUnitId}.
     */
    @Test
    public void testFindObjectLifeCyclesByUnitId() {
        when(baseLogbookRestClient.findObjectLifeCyclesByUnitId(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(JsonNodeFactory.instance.objectNode());
        assertNotNull(logbookService.findObjectLifeCyclesByUnitId(null, null));
    }

    /**
     * Test method for {@link LogbookService#findOperationById}.
     */
    @Test
    public void testFindOperationById() {
        when(baseLogbookRestClient.findOperationById(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(JsonNodeFactory.instance.objectNode());
        assertNotNull(logbookService.findOperationById(null, null));
    }

}
