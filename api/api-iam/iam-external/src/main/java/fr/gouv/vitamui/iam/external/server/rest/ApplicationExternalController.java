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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.ApplicationExternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;

/**
 * The controller to check existence, create, read, update and delete the applications.
 * No security filter on Application REST API: All user should access getAllApplications.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_APPLICATIONS_URL)
@Getter
@Setter
@Api(tags = "applications", value = "Applications Management", description = "Applications Management")
public class ApplicationExternalController implements CrudController<ApplicationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationExternalController.class);

    private final ApplicationExternalService applicationExternalService;

    @Autowired
    public ApplicationExternalController(final ApplicationExternalService applicationExternalService) {
        this.applicationExternalService = applicationExternalService;
    }

    @GetMapping
    public List<ApplicationDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("Get all with criteria={}, embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(EmbeddedOptions.class, embedded);
        return applicationExternalService.getAll(criteria, embedded);
    }

    @Override
    public ResponseEntity<Void> checkExist(String id) {
        throw new UnsupportedOperationException("check exist not implemented");
    }

    @Override
    public ApplicationDto create(final @Valid @RequestBody ApplicationDto dto) {
        throw new UnsupportedOperationException("create not implemented");
    }

    @Override
    public ApplicationDto update(final @PathVariable("id") String id, final @Valid @RequestBody ApplicationDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    public ApplicationDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        throw new UnsupportedOperationException("patch not implemented");
    }
}
