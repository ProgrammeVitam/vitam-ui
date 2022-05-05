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

package fr.gouv.vitamui.pastis.service;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.pastis.client.PastisTransformationRestClient;
import fr.gouv.vitamui.pastis.client.PastisTransformationWebClient;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.util.NoticeUtils;
import fr.gouv.vitamui.ui.commons.service.AbstractPaginateService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * UI
 * Pastis Service
 */
@Service
public class PastisTransformationService extends AbstractPaginateService<ProfileResponse> {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PastisTransformationService.class);

    private final PastisTransformationRestClient pastisTransformationRestClient;
    private final PastisTransformationWebClient pastisTransformationWebClient;
    private final ProfileService profileService;
    private CommonService commonService;

    @Autowired
    public PastisTransformationService(final PastisTransformationRestClient pastisTransformationRestClient,
        final PastisTransformationWebClient pastisTransformationWebClient,
        final ProfileService service, final CommonService commonService) {
        this.pastisTransformationRestClient = pastisTransformationRestClient;
        this.pastisTransformationWebClient = pastisTransformationWebClient;
        this.profileService = service;
        this.commonService = commonService;
    }

    @Override
    protected Integer beforePaginate(Integer page, Integer size) {
        return commonService.checkPagination(page, size);
    }

    @Override
    public BasePaginatingAndSortingRestClient<ProfileResponse, ExternalHttpContext> getClient() {
        return pastisTransformationRestClient;
    }

    public ResponseEntity<ProfileResponse> loadProfile(Notice notice, ExternalHttpContext context) throws IOException {
        LOGGER.info("Start transform profile By ui-pastis-service");
        if (notice.getControlSchema() == null) {
            Resource resource = profileService.download(context, notice.getIdentifier()).getBody();
            ElementProperties elementProperties = loadProfilePA(resource, context);
            ProfileResponse profileResponse = NoticeUtils.convertToProfileResponse(notice);
            profileResponse.setProfile(elementProperties);
            return ResponseEntity.ok(profileResponse);
        } else {
            return pastisTransformationRestClient.loadProfile(notice, context);
        }
    }

    public ElementProperties loadProfilePA(Resource resource, ExternalHttpContext context) throws IOException {
        LOGGER.info("Start transform profile PA By ui-pastis-service");
        return pastisTransformationWebClient.loadProfilePA(resource, context).getBody();
    }

    public ResponseEntity<ProfileResponse> loadProfileFromFile(MultipartFile file, ExternalHttpContext context)
        throws IOException {
        LOGGER.info("Start Upload profile By ui-pastis-service");
        return pastisTransformationWebClient.loadProfileFromFile(file, context);
    }

    public ResponseEntity<String> getArchiveProfile(final ElementProperties json, ExternalHttpContext context) {
        LOGGER.info("Start Download PA By ui-pastis-service");
        return pastisTransformationRestClient.getArchiveProfile(json, context);
    }

    public ResponseEntity<String> getArchiveUnitProfile(final ProfileNotice json, ExternalHttpContext context) {
        LOGGER.info("Start Download PUA By ui-pastis-service");
        return pastisTransformationRestClient.getArchiveUnitProfile(json, context);
    }

    public ResponseEntity<ProfileResponse> createProfile(String profileType, ExternalHttpContext context)
        throws IOException {
        LOGGER.info("Start Create profile By ui-pastis-service");
        return pastisTransformationRestClient.createProfile(profileType, context);
    }
}
