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
 * Décorateur pour les beans de l'API de l'IAM qui garantit qu'un HttpContext est toujours disponible
 * pendant l'exécution d'une méthode.
 *
 * Le HttpContext est positionné dans le HttpContextHolder avant l'invocation de la
 * méthode cible afin que les composants en aval — en particulier IamApiClient — puissent
 * récupérer les bonnes informations contextuelles (tenant, jeton utilisateur, identité,
 * identifiant d'application, etc.) au moment exact où les en-têtes HTTP sont construits.
 *
 * Cela garantit que les requêtes sortantes vers l'IAM sont enrichies de données de sécurité et de
 * tenant cohérentes et correctement délimitées, que l'appel provienne d'un
 * utilisateur authentifié ou d'un contexte technique/de service.
 */
@Slf4j
@RequiredArgsConstructor
public class IamApiDecorator {

    private final Utils utils;

    // E-mail du compte de service utilisé lorsqu'aucun utilisateur n'est authentifié (configurable).
    private final String serviceAccount;

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
        return createContext(serviceAccount);
    }

    private HttpContext createContext(String username) {
        LOGGER.debug("Building HTTP context for {}", username);
        return utils.buildContext(username);
    }
}
