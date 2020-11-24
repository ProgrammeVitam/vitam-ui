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
package fr.gouv.vitamui.referential.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.external.client.SecurityProfileExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;

@Service
public class SecurityProfileService extends AbstractPaginateService<SecurityProfileDto> {
    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SecurityProfileService.class);

    private SecurityProfileExternalRestClient client;

    private CommonService commonService;

    @Autowired
    public SecurityProfileService(final CommonService commonService, final SecurityProfileExternalRestClient client) {
        this.commonService = commonService;
        this.client = client;
    }

    @Override
    public PaginatedValuesDto<SecurityProfileDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, final ExternalHttpContext context) {
        return super.getAllPaginated(page, size, criteria, orderBy, direction, context);
    }

    @Override
    protected Integer beforePaginate(final Integer page, final Integer size) {
        return commonService.checkPagination(page, size);
    }

    @Override public BasePaginatingAndSortingRestClient<SecurityProfileDto, ExternalHttpContext> getClient() {
        return client;
    }

    public Collection<SecurityProfileDto> getAll(final ExternalHttpContext context, final Optional<String> criteria) {
        return client.getAll(context, criteria);
    }

    public boolean check(ExternalHttpContext context, SecurityProfileDto securityProfileDto) {
        return client.check(context,securityProfileDto);
    }

    public void delete(ExternalHttpContext context, String id) {
        client.delete(context, id);
    }
}
