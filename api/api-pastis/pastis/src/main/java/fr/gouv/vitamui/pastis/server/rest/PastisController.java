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

package fr.gouv.vitamui.pastis.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.server.service.PastisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

@Api(tags = "pastis")
@RequestMapping(RestApi.PASTIS)
@RestController
@ResponseBody
class PastisController {

    private static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(PastisController.class);

    @Autowired
    private PastisService profileService;

    @ApiOperation(value = "Download Pa Profile rng file")
    @Secured(ServicesData.ROLE_GET_PROFILES)
    @RequestMapping(value = RestApi.PASTIS_DOWNLOAD_PA, method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8, produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> getArchiveProfile(@RequestBody final ElementProperties json) throws IOException {
        String archiveProfile = profileService.getArchiveProfile(json);
        if (archiveProfile != null) {
            return ResponseEntity.ok(archiveProfile);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Download Pua Profile json file")
    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    @RequestMapping(value = RestApi.PASTIS_DOWNLOAD_PUA, method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getArchiveUnitProfile(@RequestBody final ProfileNotice json) throws IOException {
        String archiveUnitProfile = profileService.getArchiveUnitProfile(json);
        if (archiveUnitProfile != null) {
            return ResponseEntity.ok(archiveUnitProfile);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @ApiOperation(value = "Retrieve json representation from PUA notice")
    @Secured({ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES, ServicesData.ROLE_UPDATE_PROFILES})
    @RequestMapping(value = RestApi.PASTIS_TRANSFORM_PROFILE, method = RequestMethod.POST)
    ResponseEntity<ProfileResponse> loadProfile(@RequestBody final Notice notice)
        throws IOException {
        ProfileResponse profileResponse = profileService.loadProfile(notice);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve json representation from input file")
    @Secured({ServicesData.ROLE_CREATE_ARCHIVE_PROFILES, ServicesData.ROLE_CREATE_PROFILES})
    @RequestMapping(value = RestApi.PASTIS_UPLOAD_PROFILE, method = RequestMethod.POST,
        consumes = "multipart/form-data", produces = "application/json")
    ResponseEntity<ProfileResponse> loadProfileFromFile(@RequestParam MultipartFile file) {
        ProfileResponse profileResponse = profileService.loadProfileFromFile(file);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve json representation from PA file")
    @Secured({ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES, ServicesData.ROLE_UPDATE_PROFILES})
    @RequestMapping(value = RestApi.PASTIS_TRANSFORM_PROFILE_PA, method = RequestMethod.POST,
        consumes = "multipart/form-data", produces = "application/json")
    ResponseEntity<ElementProperties> loadPA(@RequestParam MultipartFile file) throws IOException {
        ElementProperties elementProperties = profileService.loadProfilePA(file);
        if (elementProperties != null) {
            return ResponseEntity.ok(elementProperties);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get template profile by type")
    @Secured({ServicesData.ROLE_CREATE_ARCHIVE_PROFILES, ServicesData.ROLE_CREATE_PROFILES})
    @RequestMapping(value = RestApi.PASTIS_CREATE_PROFILE, method = RequestMethod.GET)
    ResponseEntity<ProfileResponse> createProfile(@RequestParam(name = "type") String profileType)
        throws URISyntaxException, IOException {
        ProfileResponse profileResponse = profileService.createProfile(profileType);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
