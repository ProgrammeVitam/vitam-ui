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

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.ArchiveUnitInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static fr.gouv.vitamui.collect.common.rest.RestApi.UPDATE_UNITS_METADATA_PATH;

@RestController
@RequestMapping(RestApi.COLLECT_ARCHIVE_UNITS)
@Api(tags = "collect", value = "Mise à jour des meta données des unités archivistiques ")
public class ArchiveUnitsInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchiveUnitsInternalController.class);
    private final InternalSecurityService securityService;
    private final ArchiveUnitInternalService archiveUnitInternalService;

    @Autowired
    public ArchiveUnitsInternalController(final ArchiveUnitInternalService archiveUnitInternalService,
        final InternalSecurityService securityService) {
        this.securityService = securityService;
        this.archiveUnitInternalService = archiveUnitInternalService;

    }

    @PutMapping(value = "/{transactionId}" +
        UPDATE_UNITS_METADATA_PATH, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void updateArchiveUnitsMetadataFromFile(@PathVariable String transactionId, InputStream inputStream)
        throws InvalidParseOperationException, VitamClientException {
        ParameterChecker.checkParameter("The transaction Id is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("update archiveUnits metadata from file for transaction  {}", transactionId);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        archiveUnitInternalService.updateArchiveUnitsFromFile(transactionId, inputStream, vitamContext);
    }

}
