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

package fr.gouv.vitamui.commons.vitam.api.access;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Service de lancement des workflows d'élimination d'analyse et d'action
 * Pour plus d'informations : <a href="http://www.programmevitam.fr/ressources/DocCourante/autres/fonctionnel/VITAM_Eliminations.pdf">documentation métier</a>
 */
public class EliminationService {

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(EliminationService.class);

    private final AccessExternalClient accessExternalClient;

    @Autowired
    public EliminationService(final AccessExternalClient accessExternalClient) {
        this.accessExternalClient = accessExternalClient;
    }

    /**
     * Starts the elimination analysis of the units by dsl query.
     * @param vitamContext The vitam context
     * @param eliminationRequestBody The DSL query used to select units to which elimination analysis would be launched
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> startEliminationAnalysis(final VitamContext vitamContext, final EliminationRequestBody eliminationRequestBody) throws VitamClientException {
        final RequestResponse<JsonNode> response = accessExternalClient.startEliminationAnalysis(vitamContext, eliminationRequestBody);
        VitamRestUtils.checkResponse(response, HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED);
        return response;
    }

    /**
     * Starts the elimination action of the units by dsl query.
     * @param vitamContext The vitam context
     * @param eliminationRequestBody The DSL query used to select units to which elimination action would be launched
     * @return
     * @throws VitamClientException
     */
    public RequestResponse<JsonNode> startEliminationAction(final VitamContext vitamContext, final EliminationRequestBody eliminationRequestBody) throws VitamClientException {
        final RequestResponse<JsonNode> response = accessExternalClient.startEliminationAction(vitamContext, eliminationRequestBody);
        VitamRestUtils.checkResponse(response, HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED);
        return response;
    }
}
