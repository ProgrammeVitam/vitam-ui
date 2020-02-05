package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;

public interface InternalCrudControllerTest extends CrudControllerTest {

    public void testCreationFailsAsCustomerDoesNotExist() throws Exception;

    public void testUpdateFailsAsCustomerDoesNotExist() throws Exception;
}
