package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

public class AbstractService {

    private final ExternalSecurityService externalSecurityService;

    public AbstractService(ExternalSecurityService externalSecurityService) {
        this.externalSecurityService = externalSecurityService;
    }

    protected VitamContext buildVitamContext() {
        VitamContext vitamContext = externalSecurityService.getVitamContext();
        ExternalHttpContext externalHttpContext = externalSecurityService.getHttpContext();
        vitamContext.setAccessContract(externalHttpContext.getAccessContract());
        return vitamContext;
    }
}
