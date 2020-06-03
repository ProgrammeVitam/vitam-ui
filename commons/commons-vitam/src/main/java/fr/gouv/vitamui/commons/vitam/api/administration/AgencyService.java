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
package fr.gouv.vitamui.commons.vitam.api.administration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.commons.api.domain.AgencyDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;

public class AgencyService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyService.class);

    private final AdminExternalClient adminExternalClient;

    @Autowired
    public AgencyService(final AdminExternalClient adminExternalClient) {
        this.adminExternalClient = adminExternalClient;
    }

    public RequestResponse<AgenciesModel> findAgencies(final VitamContext vitamContext, final JsonNode select) throws VitamClientException {
        final RequestResponse<AgenciesModel> response = adminExternalClient.findAgencies(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<AgenciesModel> findAgencyById(final VitamContext vitamContext, final String contractId) throws VitamClientException {
        final RequestResponse<AgenciesModel> response = adminExternalClient.findAgencyByID(vitamContext, contractId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse createAgencies(final VitamContext vitamContext, final List<AgencyModelDto> agenciesModel)
            throws InvalidParseOperationException, AccessExternalClientException, IOException {
            return createAgencies(vitamContext, agenciesModel, "Agencies.json");
    }

    public RequestResponse createAgencies(final VitamContext vitamContext, final List<AgencyModelDto> accessContractModels, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = serializeAgencies(accessContractModels)) {
            return createAgencies(vitamContext, byteArrayInputStream, fileName);
        }
    }

    public RequestResponse<?> createAgencies(final VitamContext vitamContext, final InputStream accessContract, String fileName)
            throws InvalidParseOperationException, AccessExternalClientException {
        // FIXME: Check if create erase old agencies.
        // TODO: If yes, need to get all agencies, check for non-existance, add the new one, and re-import all
        return adminExternalClient.createAgencies(vitamContext, accessContract, fileName);
    }

    private ByteArrayInputStream serializeAgencies(final List<AgencyModelDto> accessContractModels) throws IOException {
        final List<AgencyDto> listOfAgencies = convertAgenciesToModelOfCreation(accessContractModels);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.convertValue(listOfAgencies, JsonNode.class);
        LOGGER.debug("The json for creation access contract, sent to Vitam {}", node);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, node);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private List<AgencyDto> convertAgenciesToModelOfCreation(final List<AgencyModelDto> AgencyModels) {
        final List<AgencyDto> listOfAC = new ArrayList<>();
        for (final AgencyModelDto aModel : AgencyModels) {
            final AgencyDto agency = new AgencyDto();
            // we don't want to inculde the tenant field in the json sent to vitam
            aModel.setTenant(null);
            listOfAC.add(VitamUIUtils.copyProperties(aModel, agency));
        }
        return listOfAC;
    }

}
