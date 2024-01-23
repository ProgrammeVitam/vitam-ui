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
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.common.dto.UnitFullPath;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.internal.server.service.GetorixDepositInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;


/**
 * The controller to check existence, create, read, update and delete the Getorix deposits.
 */
@RestController
@RequestMapping(RestApi.GETORIX_DEPOSIT_PATH)
@Api(tags = "getorix", value = "VersementGetorix")
public class GetorixDepositInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GetorixDepositInternalController.class);

    private final GetorixDepositInternalService getorixDepositInternalService;

    private final ExternalParametersService externalParametersService;

    @Autowired
    public GetorixDepositInternalController(GetorixDepositInternalService getorixDepositInternalService,
        ExternalParametersService externalParametersService) {
        this.getorixDepositInternalService = getorixDepositInternalService;
        this.externalParametersService = externalParametersService;
    }

    @ApiOperation(value = "Create Getorix Deposit")
    @PostMapping()
    public GetorixDepositDto createGetorixDeposit(final @Valid @RequestBody GetorixDepositDto getorixDepositDto)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter("the Getorix Deposit is mandatory : ", getorixDepositDto);
        SanityChecker.sanitizeCriteria(getorixDepositDto);
        LOGGER.debug("[Internal] : Create new Getorix Deposit");
        return getorixDepositInternalService.createGetorixDeposit(getorixDepositDto,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @ApiOperation(value = "Get Getorix Deposit Details")
    @GetMapping(value = CommonConstants.PATH_ID)
    public GetorixDepositDto getGetorixDepositById(final @PathVariable("id") String getorixDepositId) throws
        PreconditionFailedException {
        ParameterChecker.checkParameter("the Getorix Deposit Id is mandatory : ", getorixDepositId);
        SanityChecker.checkSecureParameter(getorixDepositId);
        LOGGER.debug("[Internal] : get the GetorixDeposit details by id : {}", getorixDepositId);
        return getorixDepositInternalService.getGetorixDepositById(getorixDepositId);
    }

    @ApiOperation(value = "Update Getorix Deposit Details")
    @PutMapping(CommonConstants.PATH_ID)
    public GetorixDepositDto updateGetorixDepositDetails(final @PathVariable("id") String getorixDepositId,
        final @RequestBody GetorixDepositDto getorixDepositDto) throws
        PreconditionFailedException {
        ParameterChecker.checkParameter("the Getorix Deposit Dto is mandatory : ", getorixDepositDto);
        ParameterChecker.checkParameter("the Getorix Deposit Id is mandatory : ", getorixDepositId);
        SanityChecker.sanitizeCriteria(getorixDepositDto);
        SanityChecker.checkSecureParameter(getorixDepositId);
        LOGGER.debug("[Internal] : update the GetorixDeposit details by id : {}", getorixDepositId);
        return getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto,
            externalParametersService.buildVitamContextFromExternalParam());
    }

    @ApiOperation(value = "Get the Archive Unit Full Path")
    @GetMapping(CommonConstants.PATH_ID + CommonConstants.FULL_PATH)
    public List<UnitFullPath> getUnitFullPath(final @PathVariable("id") String unitId) throws PreconditionFailedException,
        VitamClientException {
        ParameterChecker.checkParameter("the Archive Unit Id is mandatory : ", unitId);
        SanityChecker.checkSecureParameter(unitId);
        LOGGER.debug("[INTERNAL] : Get the full Path of the unit with Id : {}", unitId);
        return getorixDepositInternalService.getUnitFullPath(unitId, externalParametersService
            .buildVitamContextFromExternalParam());
    }

    @ApiOperation(value = "Get the last three operations")
    @GetMapping(CommonConstants.LAST_THREE_OPERATIONS)
    public List<GetorixDepositDto> getLastThreeOperations() {
        LOGGER.debug("[INTERNAL] Get the last 3 created deposits");
        final VitamContext vitamContext =  externalParametersService.buildVitamContextFromExternalParam();
        return getorixDepositInternalService.getLastThreeOperations(vitamContext);
    }
}
