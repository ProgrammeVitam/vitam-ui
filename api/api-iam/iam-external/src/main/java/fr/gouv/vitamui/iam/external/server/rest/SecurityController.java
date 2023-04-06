package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "security")
@RequestMapping("iam/v1/security")
@ResponseBody
public class SecurityController {

    @GetMapping
    @ApiOperation(value = "Get user authenticated")
    public AuthUserDto getAuthenticated() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            return (AuthUserDto) auth.getPrincipal();
        }
        throw new UnAuthorizedException("User is not connected");
    }
}
