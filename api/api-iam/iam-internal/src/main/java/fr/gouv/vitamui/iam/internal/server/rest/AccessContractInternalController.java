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
package fr.gouv.vitamui.iam.internal.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.AccessContractConverter;
import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller for Access contracts.
 */
@RestController
@RequestMapping(CommonConstants.API_VERSION_1)
@Api(tags = "accesscontracts", value = "Access contacts", description = "Access contracts Management")
public class AccessContractInternalController {

    static final Logger LOGGER = LoggerFactory.getLogger(AccessContractInternalController.class);
    protected final InternalSecurityService securityService;
    private final AccessContractService accessContractService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AccessContractInternalController(
        InternalSecurityService securityService,
        AccessContractService accessContractService,
        ObjectMapper objectMapper
    ) {
        this.securityService = securityService;
        this.accessContractService = accessContractService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/accesscontracts")
    public List<AccessContractDto> getAll() {
        final RequestResponse<AccessContractModel> requestResponse;
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());

        try {
            requestResponse = accessContractService.findAccessContracts(vitamContext, new Select().getFinalSelect());
            final AccessContractResponseDto accessContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AccessContractResponseDto.class
            );

            return AccessContractConverter.convertVitamsToDtos(accessContractResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Access Contrats", e);
        }
    }

    @GetMapping(path = "/accesscontracts/{identifier:.+}")
    public AccessContractDto getAccessContractById(final @PathVariable("identifier") String identifier)
        throws UnsupportedEncodingException {
        LOGGER.debug(
            "get accessContract identifier={} / {}",
            identifier,
            URLDecoder.decode(identifier, StandardCharsets.UTF_8)
        );
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());

        try {
            LOGGER.info("Access Contract EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
            RequestResponse<AccessContractModel> requestResponse = accessContractService.findAccessContractById(
                vitamContext,
                identifier
            );
            final AccessContractResponseDto accessContractResponseDto = objectMapper.treeToValue(
                requestResponse.toJsonNode(),
                AccessContractResponseDto.class
            );
            if (accessContractResponseDto.getResults().isEmpty()) {
                return null;
            } else {
                return AccessContractConverter.convertVitamToDto(accessContractResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get Access Contrat", e);
        }
    }
}
