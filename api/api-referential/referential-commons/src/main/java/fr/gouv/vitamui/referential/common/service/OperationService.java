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
package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.ProbativeValueRequest;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;

public class OperationService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OperationService.class);

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public OperationService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public Response exportTraceability(VitamContext context, String id) throws VitamClientException, AccessExternalClientServerException {
        LOGGER.info("Export Tracability EvIdAppSession : {} " , context.getApplicationSessionId());
        return adminExternalClient.downloadTraceabilityOperationFile(context, id);
    }
    public Response exportAudit(VitamContext context, String id) throws VitamClientException {
        LOGGER.info("Export Audit EvIdAppSession : {} " , context.getApplicationSessionId());
        return adminExternalClient.downloadBatchReport(context, id);
    }

    public void runAudit(VitamContext context, JsonNode jsonNode) throws AccessExternalClientServerException {
        LOGGER.debug("run audit {}", jsonNode);
        LOGGER.info("run audit EvIdAppSession : {} " , context.getApplicationSessionId());
        RequestResponse r = this.adminExternalClient.launchAudit(context, jsonNode);
        LOGGER.debug(r.toString());
    }

    public void lauchEvidenceAudit(VitamContext context, JsonNode jsonNode) throws VitamClientException {
        LOGGER.debug("run evidenceAudit {}", jsonNode);
        LOGGER.info("run Evidence Audit EvIdAppSession : {} " , context.getApplicationSessionId());
        RequestResponse r = this.adminExternalClient.evidenceAudit(context, jsonNode);
        LOGGER.debug(r.toString());
    }

    public void launchRectificationAudit(final VitamContext context, final String evidenceAuditIdentifier) throws VitamClientException {
        LOGGER.debug("Run rectificationAudit {}", evidenceAuditIdentifier);
        final RequestResponse r = this.adminExternalClient.rectificationAudit(context, evidenceAuditIdentifier);
        LOGGER.debug(r.toString());
    }

    public void runProbativeValue(VitamContext context, ProbativeValueRequest request) throws VitamClientException {
        LOGGER.info("run Probative Value EvIdAppSession : {} " , context.getApplicationSessionId());
        this.adminExternalClient.exportProbativeValue(context, request);
    }

}
