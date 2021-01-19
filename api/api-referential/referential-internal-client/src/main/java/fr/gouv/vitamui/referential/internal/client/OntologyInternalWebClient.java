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
package fr.gouv.vitamui.referential.internal.client;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework	.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.referential.common.rest.RestApi;

public class OntologyInternalWebClient extends BaseWebClient<InternalHttpContext>  {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OntologyInternalWebClient.class);
    
    public OntologyInternalWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }
    
    public JsonNode importOntologies(InternalHttpContext context, String fileName, MultipartFile file) {
        if (file == null) {
            LOGGER.error("No file to import");
            throw new BadRequestException("No file to import .");
        }
        
    	LOGGER.debug("Import file {}", file.getOriginalFilename());
        return multipartData(getUrl() + CommonConstants.PATH_IMPORT, HttpMethod.POST, context, 
            Collections.singletonMap("fileName", fileName),
        	Optional.of(new AbstractMap.SimpleEntry<>("file", file)), JsonNode.class);
    }
    
    @Override
    public String getPathUrl() {
        return RestApi.ONTOLOGIES_URL;
    }
}