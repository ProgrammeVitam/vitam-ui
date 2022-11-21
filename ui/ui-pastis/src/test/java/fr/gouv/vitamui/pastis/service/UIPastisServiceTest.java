package fr.gouv.vitamui.pastis.service;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.pastis.client.PastisRestClientFactory;
import fr.gouv.vitamui.pastis.client.PastisWebClientFactory;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudServiceTest;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.mockito.Mock;

public abstract class UIPastisServiceTest<T extends IdDto> extends AbstractCrudService {

    @Mock
    protected static CommonService commonService;

}
