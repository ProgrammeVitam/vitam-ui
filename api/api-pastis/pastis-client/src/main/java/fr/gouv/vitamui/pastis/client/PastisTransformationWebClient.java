package fr.gouv.vitamui.pastis.client;

import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.rest.RestApi;
import fr.gouv.vitamui.pastis.common.util.CustomMultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Optional;

public class PastisTransformationWebClient extends BaseWebClient<ExternalHttpContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PastisTransformationWebClient.class);

    public PastisTransformationWebClient(WebClient webClient, String baseUrl) {
        super(webClient, baseUrl);
    }

    public ResponseEntity<ProfileResponse> loadProfileFromFile(MultipartFile file, ExternalHttpContext context) {
        LOGGER.debug("Upload profile");
        return ResponseEntity.ok(
            multipartData(
                getUrl() + RestApi.PASTIS_UPLOAD_PROFILE,
                HttpMethod.POST,
                context,
                Collections.singletonMap("fileName", file.getOriginalFilename()),
                Optional.of(new AbstractMap.SimpleEntry<>("file", file)),
                ProfileResponse.class
            )
        );
    }

    public ResponseEntity<ElementProperties> loadProfilePA(Resource resource, ExternalHttpContext context)
        throws IOException {
        LOGGER.debug("Upload profile");
        CustomMultipartFile multipartFile = new CustomMultipartFile(resource.getInputStream().readAllBytes());
        return ResponseEntity.ok(
            multipartData(
                getUrl() + RestApi.PASTIS_TRANSFORM_PROFILE_PA,
                HttpMethod.POST,
                context,
                Collections.singletonMap("fileName", multipartFile.getOriginalFilename()),
                Optional.of(new AbstractMap.SimpleEntry<>("file", multipartFile)),
                ElementProperties.class
            )
        );
    }

    @Override
    public String getPathUrl() {
        return RestApi.PASTIS;
    }
}
