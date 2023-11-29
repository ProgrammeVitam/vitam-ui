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

package fr.gouv.vitamui.collect.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.service.GetorixDepositService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;


@Api(tags = "Getorix")
@RestController
@RequestMapping("${ui-collect.prefix}/getorix-deposit")
@Consumes("application/json")
@Produces("application/json")
public class GetorixDepositController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GetorixDepositController.class);

    private final GetorixDepositService getorixDepositService;

    @Autowired
    public GetorixDepositController(GetorixDepositService getorixDepositService) {
        this.getorixDepositService = getorixDepositService;
    }

    @ApiOperation(value = "Create new Getorix Deposit")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public GetorixDepositDto create(@RequestBody final GetorixDepositDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Getorix Deposit is mandatory", entityDto);
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("[UI] : Create new Getorix Deposit");
        return getorixDepositService.create(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "Get Getorix Deposit Details by Id")
    @GetMapping(value = CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public GetorixDepositDto getorixDepositById(final @PathVariable("id") String getorixDepositId)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter("the Getorix Deposit Id is mandatory : ", getorixDepositId);
        SanityChecker.checkSecureParameter(getorixDepositId);
        LOGGER.debug("[UI] : get the GetorixDeposit details by id : {}", getorixDepositId);
        return getorixDepositService.getOne(buildUiHttpContext(), getorixDepositId);
    }
}
