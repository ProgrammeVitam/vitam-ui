package fr.gouv.vitamui.commons.vitam.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

class AccessExternalClientEmptyMockTest {
    private AccessExternalClientEmptyMock mockClient;

    @BeforeEach
    public void setUp() {
        mockClient = new AccessExternalClientEmptyMock();
    }

    @Test
    void selectOperations() {


        // Call the selectOperations method
        var response = mockClient.selectOperations(null, null);

        // Check if the response is not null
        assertNotNull(response);

        // Check if the HTTP code is Response.Status.OK
        assertEquals(Response.Status.OK.getStatusCode(), response.getHttpCode());
    }
}
