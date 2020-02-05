package fr.gouv.vitamui.commons.test.rest;

import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;

public abstract class AbstractMockMvcCrudControllerTest<T extends IdDto> extends AbstractRestControllerMockMvcTest {

    /**
     * Method for test entity creation
     * @return ResultActions
     */
    public ResultActions testCreateEntity() {
        return testCreateEntity(status().isCreated());
    }

    /**
     * Method for test entity creation
     * @return ResultActions
     */
    public ResultActions testCreateEntity(final ResultMatcher matcher) {
        return testCreateEntity(getHeaders());

    }

    /**
     * Method for test entity update
     * @return ResultActions
     */
    public ResultActions testUpdateEntity() {
        return testUpdateEntity(getHeaders());
    }

    public void testUpdateEntityNotSupported() {
        testUpdateEntityNotSupported(getHeaders());
    }

    /**
     * Method for test get entity by id
     * @return ResultActions
     */
    public ResultActions testGetEntityById() {
        return testGetEntityById(getHeaders());
    }

    /**
     * Method for test retrieve all entity
     * @return ResultActions
     */
    public ResultActions testGetAllEntity() {
        return testGetAllEntity(Optional.empty(),getHeaders());
    }

    /**
     * Method for test retrieve all entity
     * @return ResultActions
     */
    public ResultActions testGetAllEntity(final QueryDto criteria) {
        return testGetAllEntity(Optional.ofNullable(criteria),getHeaders());
    }


    /**
     * Method for test retrieve all entity with Criteria
     * @return ResultActions
     */
    public ResultActions testGetAllEntityWithCriteria() {
        QueryDto criteria = new QueryDto();
        criteria.addCriterion(new Criterion("id", "id", CriterionOperator.EQUALS));
        return testGetAllEntity(Optional.of(criteria),getHeaders());
    }

    /**
     * Method for test patch entity
     * @return ResultActions
     */
    public ResultActions testPatchEntity() {
        return testPatchEntity(getHeaders());
    }

    /**
     * Method for test patch entity not supported
     * @return ResultActions
     */
    public void testPatchEntityNotSupported() {
        testPatchEntityNotSupported(getHeaders());
    }

    /**
     * Method for test delete entity
     * @return ResultActions
     */
    public ResultActions testDeleteEntity() {
        return testDeleteEntity(getHeaders());
    }

    /**
     * Method for test get paginated entities
     * @return
     */
    public ResultActions testGetPaginatedEntities() {
        return testGetPaginatedEntities(getHeaders());
    }

    public ResultActions testGetPaginatedEntities(final QueryDto criteria) {
        return testGetPaginatedEntities(getHeaders(),criteria);
    }


    /**
     * Method for test entity creation
     * @return ResultActions
     */
    public ResultActions testCreateEntity(final HttpHeaders httpHeaders) {
        return testCreateEntity(status().isCreated(), httpHeaders);
    }

    /**
     * Method for test entity creation
     * @return ResultActions
     */
    public ResultActions testCreateEntity(final ResultMatcher matcher, final HttpHeaders httpHeaders) {
        getLog().debug("test create class={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        ResultActions result = null;
        try {
            result = super.perform(builder, asJsonString(buildDto()), HttpMethod.POST , matcher, httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;

    }

    public void testCreateEntityNotSupported() {
        testCreateEntityNotSupported(getHeaders());
    }

    /**
     * Method for test entity creation
     * @return ResultActions
     */
    public void testCreateEntityNotSupported(final HttpHeaders httpHeaders) {
        getLog().debug("test create class={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        try {
            super.perform(builder, asJsonString(buildDto()), HttpMethod.POST , status().isMethodNotAllowed(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Method for test entity update
     * @return ResultActions
     */
    public ResultActions testUpdateEntity(final HttpHeaders httpHeaders) {
        getLog().debug("test update entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final String id = "1";
        builder.path("/" + id);
        final T entity = buildDto();
        entity.setId(id);

        ResultActions result = null;
        try {
            result = super.perform(builder, asJsonString(entity), HttpMethod.PUT , status().isOk(), httpHeaders);

        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    public void testUpdateEntityNotSupported(final HttpHeaders httpHeaders) {
        getLog().debug("test update entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final String id = "1";
        builder.path("/" + id);
        final T entity = buildDto();
        entity.setId(id);

        try {
            super.perform(builder, asJsonString(entity), HttpMethod.PUT , status().isMethodNotAllowed(), httpHeaders);

        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Method for test get entity by id
     * @return ResultActions
     */
    public ResultActions testGetEntityById(final HttpHeaders httpHeaders) {
        getLog().debug("test get entity by id class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final String id = "1";
        builder.path("/" + id);
        final T entity = buildDto();
        entity.setId(id);
        ResultActions result = null;
        try {
            result =  super.perform(builder, "", HttpMethod.GET , status().isOk(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }


    private ResultActions testGetAllEntity(final Optional<QueryDto> criteria, final HttpHeaders httpHeaders) {
        getLog().debug("test get all entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        criteria.ifPresent(c ->  builder.queryParam("criteria", c.toJson()));
        ResultActions result = null;
        try {
            result = super.perform(builder, "", HttpMethod.GET , status().isOk(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    /**
     * Method for test patch entity
     * @return ResultActions
     */
    public void testPatchEntityNotSupported(final HttpHeaders httpHeaders) {
        getLog().debug("test patch entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final Map<String, Object> updates = new HashMap<>();
        final String id = "1";
        builder.path("/" + id);
        updates.put("id", id);
        try {
            super.perform(builder,asJsonString(updates), HttpMethod.PATCH , status().isInternalServerError(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Method for test patch entity
     * @return ResultActions
     */
    public ResultActions testPatchEntity(final HttpHeaders httpHeaders) {
        getLog().debug("test patch entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final Map<String, Object> updates = new HashMap<>();
        final String id = "1";
        builder.path("/" + id);
        updates.put("id", id);
        ResultActions result = null;
        try {
            result = super.perform(builder,asJsonString(updates), HttpMethod.PATCH , status().isOk(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    /**
     * Method for test delete entity
     * @return ResultActions
     */
    public ResultActions testDeleteEntity(final HttpHeaders httpHeaders) {
        getLog().debug("test delete entity class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        final String id = "1";
        builder.path("/" + id);
        ResultActions result = null;
        try {
            result =  super.perform(builder,"",HttpMethod.DELETE,status().isNoContent(),httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    private ResultActions testGetPaginatedEntities(final HttpHeaders httpHeaders, final UriComponentsBuilder builder) {
        ResultActions result = null;
        try {
            result = super.perform(builder, "", HttpMethod.GET , status().isOk(), httpHeaders);
        }
        catch (final Exception e) {
            fail(e.getMessage());
        }
        return result;
    }

    public ResultActions testGetPaginatedEntities(final HttpHeaders httpHeaders) {
        getLog().debug("test get paginated entities class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        builder.queryParam("page", 0);
        builder.queryParam("size", "20");
        return testGetPaginatedEntities(httpHeaders,builder);
    }

    public ResultActions testGetPaginatedEntities(final HttpHeaders httpHeaders,final QueryDto criteria) {
        getLog().debug("test get paginated entities class ={}", getDtoClass().getName());
        preparedServices();
        final UriComponentsBuilder builder = getUriBuilder();
        builder.queryParam("page", 0);
        builder.queryParam("size", "20");
        builder.queryParam("criteria", criteria.toJson());
        return testGetPaginatedEntities(httpHeaders,builder);
    }

    protected abstract Class<T> getDtoClass();

    protected abstract T buildDto();

    protected abstract VitamUILogger getLog();

    protected abstract void preparedServices();

}
