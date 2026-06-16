/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.referential.server.security;

import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.security.TenantAware;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Aspect
@Component
public class TenantScopeAspect {

    private final SecurityService securityService;

    public TenantScopeAspect(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Around("@annotation(TenantScope)")
    public Object validate(ProceedingJoinPoint pjp) throws Throwable {
        Integer currentTenant = securityService.getTenantIdentifier();

        for (Object arg : pjp.getArgs()) {
            check(arg, currentTenant);
        }

        return pjp.proceed();
    }

    private void check(Object arg, Integer tenant) throws AccessDeniedException {
        if (arg == null) {
            return;
        }

        // Cas direct
        if (arg instanceof TenantAware ta) {
            if (ta.tenant() == null || !tenant.equals(ta.tenant())) {
                throw new AccessDeniedException("Tenant violation");
            }
            return;
        }

        // Collections
        if (arg instanceof Iterable<?> it) {
            for (Object o : it) {
                check(o, tenant);
            }
        }

        // Arrays optionnels
        if (arg.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(arg);
            for (int i = 0; i < len; i++) {
                check(java.lang.reflect.Array.get(arg, i), tenant);
            }
        }
    }
}
