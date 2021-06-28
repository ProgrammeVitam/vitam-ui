/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.ui.commons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.external.client.ExternalParamProfileExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  UI profile external params service.
 *
 */
@Service
public class ExternalParamProfileService extends AbstractPaginateService<ExternalParamProfileDto> {

	private final ExternalParamProfileExternalRestClient client;
    private final CommonService commonService;

	@Autowired
	public ExternalParamProfileService(final IamExternalRestClientFactory factory, final CommonService commonService) {
        this.client = factory.getExternalParamProfileExternalRestClient();
        this.commonService = commonService;
    }

    @Override
    public ExternalParamProfileDto getOne(ExternalHttpContext context, final String id) {
        return client.getOne(context, id);
    }

    @Override
    public ExternalParamProfileDto create(final ExternalHttpContext context, final ExternalParamProfileDto dto) {
        return client.create(context, dto);
    }

    @Override
    public LogbookOperationsResponseDto findHistoryById(final ExternalHttpContext context, final String id) {
        SanityChecker.check(id);
        final JsonNode body = client.findHistoryById(context, id);
        try {
            return JsonUtils.treeToValue(body, LogbookOperationsResponseDto.class, false);
        }
        catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
    }

    @Override
    protected Integer beforePaginate(Integer page, Integer size) {
        return commonService.checkPagination(page, size);
    }

    @Override
    public ExternalParamProfileExternalRestClient getClient() {
        return client;
    }

}
