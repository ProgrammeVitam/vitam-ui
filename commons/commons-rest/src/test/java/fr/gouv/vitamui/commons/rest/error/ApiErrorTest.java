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
    void testApiErrorGenerator() {
        Assertions.assertNotNull(apiErrorGenerator, "ApiErrorGenerator is null.");
    }

    @Test
    void testInvalidAuthentificationException() {
        assertThrows(InvalidAuthenticationException.class, () -> {
            throw ApiErrorGenerator.getInvalidAuthentificationException();
        });
    }

    @Test
    void testNoRightsException() {
        assertThrows(NoRightsException.class, () -> {
            throw ApiErrorGenerator.getNoRightsException();
        });
    }

    @Test
    void testBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            throw ApiErrorGenerator.getBadRequestException("bad request");
        });
    }

    @Test
    void testBadRequestExceptionWithoutArguments() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getBadRequestException();
        });
    }

    @Test
    void testInvalidFormatException() {
        assertThrows(InvalidFormatException.class, () -> {
            throw ApiErrorGenerator.getInvalidFormatException();
        });
    }

    @Test
    void testForbiddenException() {
        assertThrows(ForbiddenException.class, () -> {
            throw ApiErrorGenerator.getForbiddenException();
        });
    }

    @Test
    void testNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            throw ApiErrorGenerator.getNotFoundException();
        });
    }

    @Test
    void testRouteNotFoundExceptionWithoutArguments() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getRouteNotFoundException();
        });
    }

    @Test
    void testRouteNotFoundException() {
        assertThrows(RouteNotFoundException.class, () -> {
            throw ApiErrorGenerator.getRouteNotFoundException(HttpMethod.POST, "/path");
        });
    }

    @Test
    void testValidationException() {
        assertThrows(ValidationException.class, () -> {
            throw ApiErrorGenerator.getValidationException();
        });
    }

    @Test
    void testInternalServerException() {
        assertThrows(InternalServerException.class, () -> {
            throw ApiErrorGenerator.getInternalServerException();
        });
    }

    @Test
    void testNotImplementedException() {
        assertThrows(NotImplementedException.class, () -> {
            throw ApiErrorGenerator.getNotImplementedException();
        });
    }

    @Test
    void testRequestTimeOutException() {
        assertThrows(RequestTimeOutException.class, () -> {
            throw ApiErrorGenerator.getRequestTimeOutException();
        });
    }
}
