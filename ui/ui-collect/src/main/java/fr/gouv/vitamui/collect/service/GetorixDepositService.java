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

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.common.dto.UnitFullPath;
import fr.gouv.vitamui.collect.external.client.GetorixDepositExternalRestClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetorixDepositService extends AbstractPaginateService<GetorixDepositDto> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GetorixDepositService.class);

    private final GetorixDepositExternalRestClient getorixDepositExternalRestClient;

    @Autowired
    public GetorixDepositService(GetorixDepositExternalRestClient getorixDepositExternalRestClient) {
        this.getorixDepositExternalRestClient = getorixDepositExternalRestClient;
    }

    @Override
    protected Integer beforePaginate(Integer page, Integer size) {
        return null;
    }

    @Override
    public GetorixDepositExternalRestClient getClient() {
        return getorixDepositExternalRestClient;
    }

    @Override
    public GetorixDepositDto create(final ExternalHttpContext c, final GetorixDepositDto dto) {
        LOGGER.debug("[UI] : Create new Getorix Deposit");
        return super.create(c, dto);
    }

    @Override
    public GetorixDepositDto getOne(final ExternalHttpContext c, final String getorixDepositId) {
        LOGGER.debug("[UI] : get the GetorixDeposit details by id : {}", getorixDepositId);
        return super.getOne(c, getorixDepositId);
    }

    public GetorixDepositDto updateGetorixDepositDetails(final ExternalHttpContext c, final GetorixDepositDto getorixDepositDto) {
        LOGGER.debug("[UI] : Update the Getorix Deposit details process");
        if (StringUtils.isBlank(getorixDepositDto.getId())) {
            throw new IllegalArgumentException("Getorix Deposit identifier is mandatory.");
        }
        LOGGER.debug("[UI] : Update the Getorix Deposit details with id : {}", getorixDepositDto.getId());
        return super.update(c, getorixDepositDto);
    }

    public List<UnitFullPath> getUnitFullPath(String unitId, ExternalHttpContext context) {
        LOGGER.debug("[UI] : Get the full Path of the unit with Id : {}", unitId);
        return getorixDepositExternalRestClient.getUnitFullPath(unitId, context).getBody();
    }

    public List<GetorixDepositDto> getLastThreeOperations(ExternalHttpContext context) {
        LOGGER.debug("[UI] :Get the last 3 created deposits");
        return getorixDepositExternalRestClient.getLastThreeOperations(context).getBody();
    }

}
