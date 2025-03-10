package fr.gouv.vitamui.referential.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.security.service.SecurityService;

public class AbstractService {

    private final SecurityService securityService;

    public AbstractService(SecurityService securityService) {
        this.securityService = securityService;
    }

    protected VitamContext buildVitamContext() {
        VitamContext vitamContext = securityService.getVitamContext();
        HttpContext httpContext = securityService.getHttpContext();
        vitamContext.setAccessContract(httpContext.getAccessContract());
        return vitamContext;
    }
}
