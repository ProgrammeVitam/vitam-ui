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

package fr.gouv.vitamui.collect.external.server.service;

import fr.gouv.vitamui.collect.common.dto.JsonPatchDto;
import fr.gouv.vitamui.collect.common.dto.MultiJsonPatchDto;
import fr.gouv.vitamui.collect.common.dto.OperationIdDto;
import fr.gouv.vitamui.collect.common.dto.UpdateArchiveUnitDto;
import fr.gouv.vitamui.collect.common.rest.ArchiveUnitClient;
import fr.gouv.vitamui.collect.common.service.ArchiveUnitService;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.rest.client.AbstractHttpContext;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ArchiveUnitServiceImpl implements ArchiveUnitService {
    private final ExternalSecurityService externalSecurityService;
    private final ArchiveUnitClient client;

    @Autowired
    public ArchiveUnitServiceImpl(final ExternalSecurityService externalSecurityService,
        final ArchiveUnitClient client) {
        this.externalSecurityService = externalSecurityService;
        this.client = client;
    }

    @Override
    public OperationIdDto update(String transactionId, Set<UpdateArchiveUnitDto> updateArchiveUnitDtoSet) {
        return client.update(getContext(), transactionId, updateArchiveUnitDtoSet);
    }

    @Override
    public OperationIdDto update(String transactionId, JsonPatchDto jsonPatchDto) {
        return client.update(getContext(), transactionId, jsonPatchDto);
    }

    @Override
    public OperationIdDto update(String transactionId, MultiJsonPatchDto multiJsonPatchDto) {
        return client.update(getContext(), transactionId, multiJsonPatchDto);
    }

    private InternalHttpContext toInternalHttpContext(final ExternalHttpContext externalHttpContext) {
        final AuthUserDto user = externalSecurityService.getUser();
        final String userLevel = user.getLevel();
        if (userLevel == null) {
            throw new ApplicationServerException("Level is null for user " + user.getEmail());
        }

        final String customerId = user.getCustomerId();
        return InternalHttpContext.buildFromExternalHttpContext(externalHttpContext, customerId, userLevel);
    }

    private AbstractHttpContext getContext() {
        return toInternalHttpContext(externalSecurityService.getHttpContext());
    }
}
