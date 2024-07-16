package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;

public interface InternalCrudControllerTest extends CrudControllerTest {
    void testCreationFailsAsCustomerDoesNotExist() throws Exception;

    void testUpdateFailsAsCustomerDoesNotExist() throws Exception;
}
