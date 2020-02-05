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
package fr.gouv.vitamui.security.server.rest;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.security.common.dto.CertificateDto;
import fr.gouv.vitamui.security.common.rest.RestApi;
import fr.gouv.vitamui.security.server.certificate.service.CertificateCrudService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the certificates.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_CERTIFICATES_URL)
@Getter
@Setter
@Api(tags = "certificates", value = "Certificates Management", description = "Certificates Management")
public class CertificateCrudController implements CrudController<CertificateDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CertificateCrudController.class);

    @Autowired
    private CertificateCrudService certificateCrudService;

    @Override
    @GetMapping
    public List<CertificateDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get ALL criteria {}", criteria);
        return certificateCrudService.getAll(criteria);
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_ID, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final @PathVariable("id") String id) {
        LOGGER.debug("Check exists {}", id);
        final List<CertificateDto> dto = certificateCrudService.getMany(id);
        return RestUtils.buildBooleanResponse(dto != null && !dto.isEmpty());
    }

    @GetMapping(CommonConstants.PATH_ID)
    public CertificateDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get {} criteria={}", id, criteria);
        return certificateCrudService.getOne(id, criteria);
    }

    @Override
    @PostMapping
    public CertificateDto create(final @Valid @RequestBody CertificateDto dto) {
        LOGGER.debug("Create {}", dto);
        return certificateCrudService.create(dto);
    }

    @Override
    @PutMapping(CommonConstants.PATH_ID)
    public CertificateDto update(final @PathVariable("id") String id, final @Valid @RequestBody CertificateDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return certificateCrudService.update(dto);
    }

    @Override
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id) {
        LOGGER.debug("Delete {}", id);
        certificateCrudService.delete(id);
    }
}
