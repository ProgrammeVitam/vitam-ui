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

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.client.AbstractInternalClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileVersion;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import fr.gouv.vitamui.pastis.common.exception.TechnicalException;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.common.util.NoticeUtils;
import fr.gouv.vitamui.pastis.server.service.PastisService;
import fr.gouv.vitamui.referential.internal.client.ProfileInternalRestClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Api(tags = "pastis")
@RequestMapping(RestApi.PASTIS)
@RestController
@ResponseBody
class PastisController extends AbstractInternalClientService {

    private static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";

    private final PastisService profileService;

    private final ProfileInternalRestClient profileInternalRestClient;

    public PastisController(
        final ExternalSecurityService externalSecurityService,
        PastisService profileService,
        ProfileInternalRestClient profileInternalRestClient
    ) {
        super(externalSecurityService);
        this.profileService = profileService;
        this.profileInternalRestClient = profileInternalRestClient;
    }

    @Override
    protected BaseRestClient<InternalHttpContext> getClient() {
        return this.profileInternalRestClient;
    }

    @ApiOperation(value = "Download Pa Profile rng file")
    @Secured(ServicesData.ROLE_GET_PROFILES)
    @PostMapping(
        value = RestApi.PASTIS_DOWNLOAD_PA,
        consumes = APPLICATION_JSON_UTF8,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    ResponseEntity<String> getArchiveProfile(
        @RequestParam(name = "version", required = false) String profileVersion,
        @RequestBody final ElementProperties json
    ) throws TechnicalException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(json);
        SanityChecker.checkSecureParameter(profileVersion);
        ProfileVersion version = Objects.isNull(profileVersion)
            ? ProfileVersion.VERSION_2_1
            : ProfileVersion.fromVersionString(profileVersion);
        String archiveProfile = profileService.getArchiveProfile(json, version);
        if (archiveProfile != null) {
            return ResponseEntity.ok(archiveProfile);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Download Pua Profile json file")
    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    @PostMapping(
        value = RestApi.PASTIS_DOWNLOAD_PUA,
        consumes = APPLICATION_JSON_UTF8,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<String> getArchiveUnitProfile(@RequestBody final ProfileNotice json)
        throws TechnicalException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(json);
        String archiveUnitProfile = profileService.getArchiveUnitProfile(json, false);
        if (archiveUnitProfile != null) {
            return ResponseEntity.ok(archiveUnitProfile);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve json representation from PUA notice")
    @Secured({ ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES, ServicesData.ROLE_UPDATE_PROFILES })
    @PostMapping(value = RestApi.PASTIS_TRANSFORM_PROFILE)
    ResponseEntity<ProfileResponse> loadProfile(@RequestBody final Notice notice)
        throws TechnicalException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(notice);
        // Code copied from UI-Pastis. Cannot be in PastisService because the service is also used by Pastis Standalone
        if (notice.getControlSchema() == null) {
            Resource resource = profileInternalRestClient
                .download(getInternalHttpContext(), notice.getIdentifier())
                .getBody();
            ElementProperties elementProperties = profileService.loadProfilePA(resource);
            ProfileResponse profileResponse = NoticeUtils.convertToProfileResponse(notice);
            profileResponse.setProfile(elementProperties);
            return ResponseEntity.ok(profileResponse);
        }
        ProfileResponse profileResponse = profileService.loadProfile(notice);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve json representation from input file")
    @Secured({ ServicesData.ROLE_CREATE_ARCHIVE_PROFILES, ServicesData.ROLE_CREATE_PROFILES })
    @PostMapping(value = RestApi.PASTIS_UPLOAD_PROFILE, consumes = "multipart/form-data", produces = "application/json")
    ResponseEntity<ProfileResponse> loadProfileFromFile(@RequestParam MultipartFile file)
        throws NoSuchAlgorithmException, TechnicalException, PreconditionFailedException {
        String fileName = file.getOriginalFilename();
        SanityChecker.isValidFileName(fileName);
        ProfileResponse profileResponse = profileService.loadProfileFromFile(file, fileName, false);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve json representation from PA file")
    @Secured({ ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES, ServicesData.ROLE_UPDATE_PROFILES })
    @PostMapping(
        value = RestApi.PASTIS_TRANSFORM_PROFILE_PA,
        consumes = "multipart/form-data",
        produces = "application/json"
    )
    ResponseEntity<ElementProperties> loadPA(@RequestParam("file") MultipartFile file) {
        ElementProperties elementProperties = profileService.loadProfilePA(file);
        if (elementProperties != null) {
            return ResponseEntity.ok(elementProperties);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get template profile by type")
    @Secured({ ServicesData.ROLE_CREATE_ARCHIVE_PROFILES, ServicesData.ROLE_CREATE_PROFILES })
    @GetMapping(value = RestApi.PASTIS_CREATE_PROFILE)
    ResponseEntity<ProfileResponse> createProfile(
        @RequestParam(name = "type") String profileType,
        @RequestParam(name = "version", required = false) String profileVersion
    ) throws NoSuchAlgorithmException, TechnicalException, InvalidParseOperationException {
        SanityChecker.checkSecureParameter(profileType);
        SanityChecker.checkSecureParameter(profileVersion);
        ProfileType type = Objects.isNull(profileType) ? null : ProfileType.valueOf(profileType);
        ProfileVersion version = Objects.isNull(profileVersion)
            ? ProfileVersion.VERSION_2_1
            : ProfileVersion.fromVersionString(profileVersion);
        ProfileResponse profileResponse = profileService.createProfile(type, version, false);
        if (profileResponse != null) {
            return ResponseEntity.ok(profileResponse);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get meta-model by type and version")
    @Secured({ ServicesData.ROLE_GET_ARCHIVE_PROFILES, ServicesData.ROLE_GET_PROFILES })
    @GetMapping(value = RestApi.PASTIS_METAMODEL)
    ResponseEntity<SedaNode> getMetaModel(@RequestParam(name = "version") String version) throws IOException {
        SanityChecker.checkSecureParameter(version);
        ProfileVersion profileVersion = ProfileVersion.fromVersionString(version);
        SedaNode sedaNode = profileService.getMetaModel(profileVersion);
        if (sedaNode == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sedaNode);
    }
}
