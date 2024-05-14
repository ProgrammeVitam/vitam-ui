/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.ui.commons.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.AccessContractsDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.ui.commons.service.AccessContractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Api(tags = "access contracts")
@RestController
@RequestMapping("${ui-prefix}/accesscontracts")
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
public class AccessContractController extends AbstractUiRestController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessContractController.class);
    private final AccessContractService accessContractService;

    @Autowired
    public AccessContractController(final AccessContractService service) {
        this.accessContractService = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccessContractsDto> getAll() throws InvalidParseOperationException, PreconditionFailedException {
        return accessContractService.getAll(buildUiHttpContext());
    }

    @ApiOperation(value = "Get access contract by ID")
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public AccessContractsDto getById(final @PathVariable("identifier") String identifier)
        throws UnsupportedEncodingException, InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug(
            "get access contract by id {} / {}",
            identifier,
            URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString())
        );
        return accessContractService.getAccessContractById(
            buildUiHttpContext(),
            URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString())
        );
    }
}
