package fr.gouv.vitamui.commons.test.rest;

public interface CrudControllerTest {

    void testCreationOK() throws Exception;

    void testCreationFailsAsIdIsProvided() throws Exception;

    void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception;

    void testUpdateOK() throws Exception;

}
