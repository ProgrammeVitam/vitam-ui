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
package fr.gouv.vitamui.referential.internal.server.rest;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.unit.UnitInternalService;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping(RestApi.UNITS_PATH)
@Getter
@Setter
public class UnitInternalController {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UnitInternalController.class);

    private static final String[] FILING_PLAN_PROJECTION = new String[] { "#id", "Title", "DescriptionLevel", "#unitType", "#unitups", "#allunitups", "Keyword", "Vtag" };

    @Autowired
    private UnitInternalService unitInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(CommonConstants.PATH_ID)
    public JsonNode findUnitById(
            @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
            @PathVariable final String id) throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return unitInternalService.findUnitById(id, vitamContext);
    }
 
    // TODO : Secure it !
    @PostMapping({RestApi.DSL_PATH, RestApi.DSL_PATH + CommonConstants.PATH_ID})
    public JsonNode findByDsl(
            @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
            @PathVariable final Optional<String> id,
            @RequestBody final JsonNode dsl) throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return unitInternalService.searchUnitsWithErrors(id, dsl, vitamContext);
    }
    
    @PostMapping(CommonConstants.PATH_ID + CommonConstants.PATH_OBJECTS)
    public JsonNode findObjectMetadataById(            
    		@RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
            @PathVariable final String id,
            @RequestBody final JsonNode dsl) throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return unitInternalService.findObjectMetadataById(id, dsl, vitamContext);
    }

    // TODO: Secure it ! Multiple (OR) CREATE_APPNAME_ROLE ? Unique FILLING_PLAN_ACCESS ?
    @GetMapping(RestApi.FILING_PLAN_PATH)
    public VitamUISearchResponseDto getFillingPlan(
            @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
            throws VitamClientException, IOException {
        LOGGER.debug("Get filing plan");
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        
        // TULEAP-20359 : The filling plan must retrieve the units with the FILING or HOLDING type
        final JsonNode fillingOrHoldingQuery = createQueryForFillingOrHoldingUnit();
        
        return objectMapper.treeToValue(unitInternalService.searchUnits(fillingOrHoldingQuery, vitamContext), VitamUISearchResponseDto.class);
    }

    private JsonNode createQueryForFillingOrHoldingUnit() {
        try {
            final SelectMultiQuery select = new SelectMultiQuery();
            final Query query = in(unitType(), UnitTypeEnum.FILING_UNIT.getValue(), UnitTypeEnum.HOLDING_UNIT.getValue());
            select.addQueries(query);
            select.addUsedProjection(FILING_PLAN_PROJECTION);
            return select.getFinalSelect();
        }
        catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new UnexpectedDataException("Unexpected error occured while building holding dsl query : " + e.getMessage());
        }
    }

}
