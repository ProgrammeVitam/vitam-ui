/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.service.PastisTransformationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.IOException;


@Api(tags = "pastis")
@RestController
@RequestMapping("${ui-pastis.prefix}" + RestApi.PASTIS)
@Consumes("application/json")
@Produces("application/json")
public class PastisController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PastisController.class);

    private final PastisTransformationService pastisTransformationService;

    @Autowired
    public PastisController(final PastisTransformationService pastisTransformationService) {
        this.pastisTransformationService = pastisTransformationService;
    }

    @ApiOperation(value = "Transform profile")
    @PostMapping(value = RestApi.PASTIS_TRANSFORM_PROFILE)
    ResponseEntity<ProfileResponse> loadProfile(@RequestBody final Notice notice) throws IOException, InvalidParseOperationException {
        LOGGER.debug("Start get profile By ui-pastis-controller");
        return pastisTransformationService.loadProfile(notice, buildUiHttpContext());
    }

    @ApiOperation(value = "Upload Profile Vitamui")
    @PostMapping(RestApi.PASTIS_UPLOAD_PROFILE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ProfileResponse> loadProfileFromFile(@RequestParam("file") MultipartFile file)
        throws IOException, InvalidParseOperationException {
        LOGGER.debug("Start Upload profile By ui-pastis-controller");
        return pastisTransformationService.loadProfileFromFile(file, buildUiHttpContext());
    }

    @ApiOperation(value = "Download Archive Profile")
    @PostMapping(RestApi.PASTIS_DOWNLOAD_PA)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<String> getArchiveProfile(@RequestBody final ElementProperties json) throws IOException, InvalidParseOperationException {
        LOGGER.debug("Start download PA By ui-pastis-controller");
        return pastisTransformationService.getArchiveProfile(json, buildUiHttpContext());
    }

    @ApiOperation(value = "Download Archive Unit Profile")
    @PostMapping(RestApi.PASTIS_DOWNLOAD_PUA)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<String> getArchiveUnitProfile(@RequestBody final ProfileNotice json) throws IOException, InvalidParseOperationException {
        LOGGER.debug("Start download PUA By ui-pastis-controller");
        return pastisTransformationService.getArchiveUnitProfile(json, buildUiHttpContext());
    }

    @ApiOperation(value = "Create new Profile by type PA or PUA")
    @GetMapping(RestApi.PASTIS_CREATE_PROFILE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ProfileResponse> createProfile(@RequestParam(name = "type") String profileType) throws IOException, InvalidParseOperationException {
        LOGGER.debug("Create new Profile by type PA or PUA By ui-pastis-controller");
        return pastisTransformationService.createProfile(profileType, buildUiHttpContext());
    }
}
