package fr.gouv.vitamui.referential.internal.client;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.ws.rs.BadRequestException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Optional;

public class AccessContractInternalWebClient extends BaseWebClient<InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessContractInternalWebClient.class);

    public AccessContractInternalWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    public ResponseEntity<Void> importAccessContracts(InternalHttpContext context, MultipartFile file) {
        if (file == null) {
            throw new BadRequestException("No file to import .");
        }

        LOGGER.debug("Import file {}", file.getOriginalFilename());
        return multipartData(getUrl() + CommonConstants.PATH_IMPORT, HttpMethod.POST, context,
            Collections.singletonMap("fileName", file.getOriginalFilename()),
            Optional.of(new AbstractMap.SimpleEntry<>("file", file)), ResponseEntity.class);
    }

    @Override
    public String getPathUrl() {
        return RestApi.ACCESS_CONTRACTS_URL;
    }
}
