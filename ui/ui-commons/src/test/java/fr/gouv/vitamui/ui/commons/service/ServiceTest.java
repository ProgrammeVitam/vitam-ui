package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import org.mockito.Mock;

public abstract class ServiceTest<T extends IdDto> extends AbstractCrudServiceTest<T> {

    @Mock
    protected static CommonService commonService;

    @Mock
    protected static IamExternalRestClientFactory factory;
}
