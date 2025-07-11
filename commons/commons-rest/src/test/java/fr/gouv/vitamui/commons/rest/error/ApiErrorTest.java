package fr.gouv.vitamui.commons.rest.error;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import fr.gouv.vitamui.commons.api.exception.RouteNotFoundException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import jakarta.ws.rs.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@Import(ApiErrorGenerator.class)
public class ApiErrorTest {

    @Autowired
    private ApiErrorGenerator apiErrorGenerator;

    @Test
    public void testApiErrorGenerator() {
        Assertions.assertNotNull(apiErrorGenerator, "ApiErrorGenerator is null.");
    }

    @Test
    public void testInvalidAuthentificationException() {
        assertThrows(InvalidAuthenticationException.class, () -> {
            throw ApiErrorGenerator.getInvalidAuthentificationException();
        });
    }

    @Test
    public void testNoRightsException() {
        assertThrows(NoRightsException.class, () -> {
            throw ApiErrorGenerator.getNoRightsException();
        });
    }

    @Test
    public void testBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            throw ApiErrorGenerator.getBadRequestException("bad request");
        });
    }

    @Test
    public void testBadRequestExceptionWithoutArguments() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getBadRequestException();
        });
    }

    @Test
    public void testInvalidFormatException() {
        assertThrows(InvalidFormatException.class, () -> {
            throw ApiErrorGenerator.getInvalidFormatException();
        });
    }

    @Test
    public void testForbiddenException() {
        assertThrows(ForbiddenException.class, () -> {
            throw ApiErrorGenerator.getForbiddenException();
        });
    }

    @Test
    public void testNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            throw ApiErrorGenerator.getNotFoundException();
        });
    }

    @Test
    public void testRouteNotFoundExceptionWithoutArguments() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getRouteNotFoundException();
        });
    }

    @Test
    public void testRouteNotFoundException() {
        assertThrows(RouteNotFoundException.class, () -> {
            throw ApiErrorGenerator.getRouteNotFoundException(HttpMethod.POST, "/path");
        });
    }

    @Test
    public void testValidationException() {
        assertThrows(ValidationException.class, () -> {
            throw ApiErrorGenerator.getValidationException();
        });
    }

    @Test
    public void testInternalServerException() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getInternalServerException();
        });
    }

    @Test
    public void testNotImplementedException() {
        assertThrows(NotImplementedException.class, () -> {
            throw ApiErrorGenerator.getNotImplementedException();
        });
    }

    @Test
    public void testRequestTimeOutException() {
        assertThrows(RequestTimeOutException.class, () -> {
            throw ApiErrorGenerator.getRequestTimeOutException();
        });
    }
}
