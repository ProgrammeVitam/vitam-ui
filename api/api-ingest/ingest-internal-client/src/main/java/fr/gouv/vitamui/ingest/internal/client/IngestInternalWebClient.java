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
package fr.gouv.vitamui.ingest.internal.client;

import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.FileOperationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.BaseWebClient;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.Optional;

/**
 * External WebClient for Ingest operations.
 *
 *
 */
public class IngestInternalWebClient extends BaseWebClient<InternalHttpContext> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestInternalWebClient.class);

    public IngestInternalWebClient(final WebClient webClient, final String baseUrl) {
        super(webClient, baseUrl);
    }

    public Mono<RequestResponseOK> upload(final InternalHttpContext context, InputStream in, final String action,
        final String contextId) {

        if (in == null) {
            throw new FileOperationException("There is an error in uploaded file !");
        }

        final Path tmpFilePath = Paths.get(FileUtils.getTempDirectoryPath(), context.getRequestId());
        int length = 0;
        try {
            length = in.available();
            Files.copy(in, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.debug("[IngestInternalWebClient] Error writing InputStream of length [{}] to temporary path {}",
                length, tmpFilePath.toAbsolutePath());
            throw new BadRequestException("ERROR: InputStream writing error : ", e);
        }

        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(CommonConstants.X_CONTEXT_ID, contextId);
        headers.add(CommonConstants.X_ACTION, action);

        return multipartDataFromFile(getPathUrl() + CommonConstants.INGEST_UPLOAD, HttpMethod.POST, context,
            Optional.of(new AbstractMap.SimpleEntry<>(CommonConstants.MULTIPART_FILE_PARAM_NAME, tmpFilePath)),
            headers)
            .bodyToMono(RequestResponseOK.class);
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_INGEST;
    }
}
