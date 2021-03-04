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

package fr.gouv.vitamui.archive.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archive.internal.server.service.ArchiveSearchInternalService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;

@RestController
@RequestMapping(RestApi.ARCHIVE_SEARCH_PATH)
@Getter
@Setter
@Api(tags = "archives search", value = "Archives units search")
public class ArchiveSearchInternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalController.class);

    private static final String[] FILING_PLAN_PROJECTION =
        new String[] {"#id", "Title", "Title_", "DescriptionLevel", "#unitType", "#unitups", "#allunitups"};

    private ArchiveSearchInternalService archiveInternalService;

    private InternalSecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public ArchiveSearchInternalController(final ArchiveSearchInternalService archiveInternalService,
        final InternalSecurityService securityService) {
        this.archiveInternalService = archiveInternalService;
        this.securityService = securityService;
    }

    @PostMapping(RestApi.SEARCH_PATH)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @RequestBody final SearchCriteriaDto searchQuery) throws VitamClientException, IOException {
        LOGGER.info("Calling service searchArchiveUnits for tenantId {}, accessContractId {} By Criteria {} ", tenantId,
            accessContractId, searchQuery);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return archiveInternalService.searchArchiveUnitsByCriteria(searchQuery, vitamContext);
    }

    @GetMapping(RestApi.FILING_HOLDING_SCHEME_PATH)
    public VitamUISearchResponseDto getFillingHoldingScheme(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException, IOException {
        LOGGER.debug("Get filing plan");
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        final JsonNode holdingQuery = createQueryForHoldingUnit();
        return objectMapper.treeToValue(archiveInternalService.searchUnits(holdingQuery, vitamContext),
            VitamUISearchResponseDto.class);
    }

    private JsonNode createQueryForHoldingUnit() {
        try {
            final SelectMultiQuery select = new SelectMultiQuery();
            final Query query =
                in(unitType(), UnitTypeEnum.HOLDING_UNIT.getValue(), UnitTypeEnum.FILING_UNIT.getValue());
            select.addQueries(query);
            select.addUsedProjection(FILING_PLAN_PROJECTION);
            LOGGER.debug("query =", select.getFinalSelect().toPrettyString());
            return select.getFinalSelect();
        } catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new UnexpectedDataException(
                "Unexpected error occured while building holding dsl query : " + e.getMessage());
        }

    }

    @GetMapping(RestApi.ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public ResultsDto findUnitById(final @PathVariable("id") String id, @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException {
        LOGGER.info("UA Details  {}", id);
        VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        return archiveInternalService.findUnitById(id,vitamContext);
    }

    @GetMapping(RestApi.DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> downloadObjectFromUnit( final @PathVariable("id") String id,
        @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId)
        throws VitamClientException {

        ResponseEntity<Resource> result = null;

        LOGGER.info("Access Contract {} ", accessContractId);
        LOGGER.info("Download Archive Unit Object with id  {}", id);
       final  VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier(), accessContractId);
        Response response =  archiveInternalService.downloadObjectFromUnit(id, vitamContext);
              Object entity = response.getEntity();
              if (entity instanceof InputStream) {
                  Resource resource = new InputStreamResource((InputStream) entity);
                  result = new ResponseEntity<>(resource, HttpStatus.OK);
              }
              return result;
}
}
