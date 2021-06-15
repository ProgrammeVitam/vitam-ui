package fr.gouv.vitamui.ingest.thread;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.ingest.external.client.IngestExternalWebClient;
import fr.gouv.vitamui.ingest.service.IngestService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Thread that send the uploaded stream to vitamui ingest-external through it's client
 */
public class IngestThread extends Thread {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestService.class);

    private final IngestExternalWebClient client;

    private final ExternalHttpContext context;

    private final String originalFilename;

    private final String contextId;
    private final String action;
    private final InputStream in;

    public IngestThread(final IngestExternalWebClient client, final ExternalHttpContext context, InputStream in,
        final String contextId, final String action, final String originalFilename) {
        this.client = client;
        this.originalFilename = originalFilename;
        this.context = context;
        this.contextId = contextId;
        this.action = action;
        this.in = in;
    }


    @Override
    public void run() {

        /*
        ClientResponse response = null;
        try {
            response = client.upload(context, in, contextId, action, originalFilename);
            if (!response.statusCode().is2xxSuccessful()) {
                LOGGER.debug("Upload of [{}] failed. StatusCode : [{}] .", originalFilename,
                    response.statusCode());
                deleteTempFiles();
            }

            if (response.statusCode().is2xxSuccessful()) {
                LOGGER.debug("Upload of [{}] succeeded with StatusCode : [{}].", originalFilename,
                    response.statusCode());
                deleteTempFiles();
            }
        } catch (final Exception e) {
            LOGGER.debug("ERROR : Upload of [{}] failed.\n [{}]", originalFilename, e.getMessage());
        }


         */

        final Path tmpFilePath =
            Paths.get(System.getProperty("java.io.tmpdir"), originalFilename);
        int length = 0;
        try {
            length = in.available();
            Files.copy(in, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.debug("[IngestInternalWebClient] Error writing InputStream of length [{}] to temporary path {}",
                length, tmpFilePath.toAbsolutePath());
            throw new BadRequestException("ERROR: InputStream writing error : ", e);
        }
    }

    private void deleteTempFiles() {
        try {
            LOGGER.info("Try to delete temp file {} ", originalFilename);
            Files.deleteIfExists(
                Paths.get(System.getProperty(CommonConstants.VITAMUI_TEMP_DIRECTORY), context.getRequestId()));
            Files.deleteIfExists(
                Paths.get(System.getProperty(CommonConstants.VITAMUI_TEMP_DIRECTORY),
                    "int-" + context.getRequestId()));
            Files.deleteIfExists(
                Paths.get(System.getProperty(CommonConstants.VITAMUI_TEMP_DIRECTORY),
                    "ext-" + context.getRequestId()));
        } catch (IOException e) {
            LOGGER.error("Error deleting temp file {} error {} ", originalFilename, e.getMessage());
        }
    }

}
