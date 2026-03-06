package fr.gouv.vitamui.cas.util;

import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.rest.client.HttpContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.EMAIL_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;

/**
 * Decorator for IAM API beans that ensures an HttpContext is always available
 * during method execution.
 *
 * The HttpContext is positioned in the HttpContextHolder before invoking the
 * target method so that downstream components—especially IamApiClient—can
 * retrieve the correct contextual information (tenant, user token, identity,
 * application id, etc.) at the exact time HTTP headers are built.
 *
 * This guarantees that outgoing IAM requests are enriched with consistent and
 * properly scoped security and tenant data, whether the call originates from
 * an authenticated user or from a technical/service context.
 */
@Slf4j
@RequiredArgsConstructor
public class IamApiDecorator {

    private static final String DEFAULT_SERVICE_ACCOUNT = "admin@change-it.fr";

    private final Utils utils;

    @SuppressWarnings("unchecked")
    public <T> T decorate(T target) {
        ProxyFactory factory = new ProxyFactory(target);

        factory.addAdvice(
            (MethodInterceptor) invocation -> {
                Optional<HttpContext> existingContext = HttpContextHolder.get();
                if (existingContext.isPresent()) {
                    LOGGER.debug("HTTP context already present: {}", existingContext.get());
                    return invocation.proceed();
                }

                HttpContext context = resolveUserContext().orElseGet(this::createServiceContext);

                try (var ignored = HttpContextHolder.setInScope(context)) {
                    return invocation.proceed();
                }
            }
        );

        return (T) factory.getProxy();
    }

    private Optional<HttpContext> resolveUserContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext == null || securityContext.getAuthentication() == null) {
            return Optional.empty();
        }

        Object principal = securityContext.getAuthentication().getPrincipal();
        if (principal == null) {
            return Optional.empty();
        }

        return extractUsername(principal).map(this::createContext);
    }

    private Optional<String> extractUsername(Object principal) {
        if (principal instanceof Principal casPrincipal) {
            Map<String, List<Object>> attributes = casPrincipal.getAttributes();
            String principalEmail = (String) utils.getAttributeValue(attributes, EMAIL_ATTRIBUTE);
            String superUserEmail = (String) utils.getAttributeValue(attributes, SUPER_USER_ATTRIBUTE);
            String superUserCustomerId = (String) utils.getAttributeValue(attributes, SUPER_USER_CUSTOMER_ID_ATTRIBUTE);
            String username = StringUtils.isNotBlank(superUserCustomerId) ? superUserEmail : principalEmail;

            return Optional.ofNullable(username);
        }

        if (principal instanceof String username) {
            return Optional.of(username);
        }

        return Optional.empty();
    }

    private HttpContext createServiceContext() {
        LOGGER.debug("Using service account context");
        return createContext(DEFAULT_SERVICE_ACCOUNT);
    }

    private HttpContext createContext(String username) {
        LOGGER.debug("Building HTTP context for {}", username);
        return utils.buildContext(username);
    }
}
