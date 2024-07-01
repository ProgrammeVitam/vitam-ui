package fr.gouv.vitamui.pastis.service;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.mockito.Mock;

public abstract class UIPastisServiceTest<T extends IdDto> extends AbstractCrudService {

    @Mock
    protected static CommonService commonService;
}
