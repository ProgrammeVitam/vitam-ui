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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.agency.AgencyInternalService;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping(RestApi.AGENCIES_URL)
@Getter
@Setter
public class AgencyInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyInternalController.class);

    @Autowired
    private AgencyInternalService agencyInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping()
    public Collection<AgencyDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return agencyInternalService.getAll(vitamContext);
    }

    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<AgencyDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return agencyInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public AgencyDto getOne(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("get agency identifier={} / {}", identifier, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return agencyInternalService.getOne(vitamContext, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
    }

    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> checkExist(@RequestBody AgencyDto accessContractDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist accessContract={}", accessContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        accessContractDto.setTenant(tenant);
        final boolean exist = agencyInternalService.check(vitamContext, accessContractDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PostMapping
    public AgencyDto create(@Valid @RequestBody AgencyDto accessContractDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("create accessContract={}", accessContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        accessContractDto.setTenant(tenant);
        return agencyInternalService.create(vitamContext,accessContractDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    public AgencyDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return agencyInternalService.patch(vitamContext, partialDto);
    }

    @DeleteMapping(CommonConstants.PATH_ID)
    public ResponseEntity<Boolean> delete(final @PathVariable("id") String id) {
        LOGGER.debug("Delete {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return new ResponseEntity<>(agencyInternalService.delete(vitamContext, id), HttpStatus.OK);
    }

    @GetMapping(CommonConstants.PATH_EXPORT)
    public ResponseEntity<Resource> export() {
        LOGGER.debug("Export Agencies");
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());

        Response response = agencyInternalService.export(vitamContext);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream)entity);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("get logbook for agency with id :{}", id);
        return agencyInternalService.findHistoryByIdentifier(vitamContext, id);
    }
    
    @PostMapping(CommonConstants.PATH_IMPORT)
    public JsonNode importAgencies(@RequestParam("fileName") String fileName, @RequestParam("file") MultipartFile file) {
        LOGGER.debug("import agency file {}", fileName);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());	
        return agencyInternalService.importAgencies(vitamContext, fileName, file);
    }
}
