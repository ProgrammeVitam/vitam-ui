package fr.gouv.vitamui.commons.rest.error;

import javax.ws.rs.HttpMethod;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.RouteNotFoundException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(ApiErrorGenerator.class)
public class ApiErrorTest {

    @Autowired
    private ApiErrorGenerator apiErrorGenerator;

    @Test
    public void testApiErrorGenerator() {
        Assert.assertNotNull("ApiErrorGenerator is null.", apiErrorGenerator);
    }

    @Test(expected = InvalidAuthenticationException.class)
    public void testInvalidAuthentificationException() {
        throw ApiErrorGenerator.getInvalidAuthentificationException();
    }

    @Test(expected = NoRightsException.class)
    public void testNoRightsException() {
        throw ApiErrorGenerator.getNoRightsException();
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestException() {
        throw ApiErrorGenerator.getBadRequestException("bad request");
    }

    @Test(expected = InternalServerException.class)
    public void testBadRequestExceptionWithoutArguments() {
        throw ApiErrorGenerator.getBadRequestException();
    }

    @Test(expected = InvalidFormatException.class)
    public void testInvalidFormatException() {
        throw ApiErrorGenerator.getInvalidFormatException();
    }

    @Test(expected = ForbiddenException.class)
    public void testForbiddenException() {
        throw ApiErrorGenerator.getForbiddenException();
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundException() {
        throw ApiErrorGenerator.getNotFoundException();
    }

    @Test(expected = InternalServerException.class)
    public void testRouteNotFoundExceptionWithoutArguments() {
        throw ApiErrorGenerator.getRouteNotFoundException();
    }

    @Test(expected = RouteNotFoundException.class)
    public void testRouteNotFoundException() {
        throw ApiErrorGenerator.getRouteNotFoundException(HttpMethod.POST, "/path");
    }

    @Test(expected = ValidationException.class)
    public void testValidationException() {
        throw ApiErrorGenerator.getValidationException();
    }

    @Test(expected = InternalServerException.class)
    public void testInternalServerException() {
        throw ApiErrorGenerator.getInternalServerException();
    }

    @Test(expected = NotImplementedException.class)
    public void testNotImplementedException() {
        throw ApiErrorGenerator.getNotImplementedException();
    }

}
