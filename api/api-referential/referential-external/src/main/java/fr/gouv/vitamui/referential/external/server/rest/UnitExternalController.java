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
package fr.gouv.vitamui.referential.external.server.rest;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.UnitExternalService;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@RestController
@RequestMapping(RestApi.UNITS_PATH)
@Getter
@Setter
public class UnitExternalController {

    @Autowired
    private UnitExternalService unitExternalService;

    @GetMapping(CommonConstants.PATH_ID)
    public VitamUISearchResponseDto searchById(final @PathVariable("id") String id) {
        ParameterChecker.checkParameter("The archive unit id is mandatory : ", id);
        return unitExternalService.findUnitById(id);
    }

    @PostMapping({RestApi.DSL_PATH, RestApi.DSL_PATH + CommonConstants.PATH_ID})
    @Secured(ServicesData.ROLE_GET_UNITS)
    public JsonNode searchByDsl(final @PathVariable Optional<String> id, final @RequestBody JsonNode dsl) {
        ParameterChecker.checkParameter("The dsl query is mandatory : ", dsl);
        SanityChecker.sanitizeCriteria(Optional.of(dsl.toString()));
        return unitExternalService.findUnitByDsl(id, dsl);
    }
    
    @PostMapping(CommonConstants.PATH_ID + CommonConstants.PATH_OBJECTS)
    @Secured(ServicesData.ROLE_GET_UNITS)
    public JsonNode findObjectMetadataById(            
            @PathVariable final String id,
            @RequestBody final JsonNode dsl) throws VitamClientException {
        ParameterChecker.checkParameter("The dsl query is mandatory : ", dsl);
        SanityChecker.sanitizeCriteria(Optional.of(dsl.toString()));
        return unitExternalService.findObjectMetadataById(id, dsl);
    }

    // TODO: Must Secure ? Multiple (OR) CREATE_APPNAME_ROLE ? Unique FILLING_PLAN_ACCESS ?
    @GetMapping(RestApi.FILING_PLAN_PATH)
    public VitamUISearchResponseDto getFillingPlan() {
        return unitExternalService.getFilingPlan();
    }

}
