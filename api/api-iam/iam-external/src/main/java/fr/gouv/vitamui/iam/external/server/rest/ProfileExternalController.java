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
package fr.gouv.vitamui.iam.external.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ProfileExternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the profiles.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_PROFILES_URL)
@Getter
@Setter
@Api(tags = "profiles", value = "Profiles Management", description = "Profiles Management")
public class ProfileExternalController implements CrudController<ProfileDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileExternalController.class);

    private final ProfileExternalService profileExternalService;

    @Autowired
    public ProfileExternalController(final ProfileExternalService profileExternalService) {
        this.profileExternalService = profileExternalService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_PROFILES)
    public Collection<ProfileDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("Get all with criteria={}, embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return profileExternalService.getAll(criteria, embedded);
    }

    @Override
    @Secured(ServicesData.ROLE_GET_PROFILES)
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        RestUtils.checkCriteria(Optional.of(criteria));
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = profileExternalService.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_PROFILES)
    public ProfileDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get {}, embedded={}", id, embedded);
        SanityChecker.check(id);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return profileExternalService.getOne(id, embedded);
    }

    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_PROFILES)
    @Override
    public ProfileDto create(final @Valid @RequestBody ProfileDto dto) {
        LOGGER.debug("Create {}", dto);
        return profileExternalService.create(dto);
    }

    @Override
    public ProfileDto update(final @PathVariable("id") String id, final @Valid @RequestBody ProfileDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @Secured(ServicesData.ROLE_GET_PROFILES)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<ProfileDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction, @RequestParam(required = false) final Optional<String> embedded) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}, embedded = {}", page, size, criteria, orderBy, direction, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        final PaginatedValuesDto<ProfileDto> result = profileExternalService.getAllPaginated(page, size, criteria, orderBy, direction, embedded);

        return result;
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_PROFILES)
    public ProfileDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");

        return profileExternalService.patch(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for user with id :{}", id);
        return profileExternalService.findHistoryById(id);
    }

    /**
     * Get levels by criteria.
     * @param criteria Criteria as json string
     * @return List of matching levels
     */
    @GetMapping(CommonConstants.PATH_LEVELS)
    @Secured(ServicesData.ROLE_GET_PROFILES)
    public List<String> getLevels(final Optional<String> criteria) {
        LOGGER.debug("Get levels with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return profileExternalService.getLevels(criteria);
    }
}
