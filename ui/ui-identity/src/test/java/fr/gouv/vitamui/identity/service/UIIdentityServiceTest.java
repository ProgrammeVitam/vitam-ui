package fr.gouv.vitamui.identity.service;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudServiceTest;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.mockito.Mock;

public abstract class UIIdentityServiceTest<T extends IdDto> extends AbstractCrudServiceTest<T> {

    @Mock
    protected static CommonService commonService;

    @Mock
    protected static IamExternalRestClientFactory factory;
}
