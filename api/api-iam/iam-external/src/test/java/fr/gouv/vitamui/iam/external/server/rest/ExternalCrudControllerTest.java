package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;

public interface ExternalCrudControllerTest extends CrudControllerTest {
    void testCreationFailsAsCustomerDoesNotExist() throws Exception;

    void testUpdateFailsAsCustomerDoesNotExist() throws Exception;
}
