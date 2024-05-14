package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.AbstractHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

public class TestUri {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TestUri.class);

    public static void main(final String[] args) throws IOException {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        builder.queryParam("test", OffsetDateTime.now().toString());

        LOGGER.info("Builder toString " + builder.build().toUri());
        LOGGER.info("Builder toString toUriString : " + builder.build().toUriString());
        LOGGER.info("Builder toString Encode toUriString : " + builder.build().encode().toUriString());

        final URIBuilder uri = new URIBuilder().setScheme("http").setHost("www.google.com");
        uri.addParameter("test", OffsetDateTime.now().toString());
        try {
            LOGGER.info("Apache Builder toString " + uri.build().getRawQuery());
        } catch (final URISyntaxException e) {
            LOGGER.error("Error Building URL : ", e);
        }

        final InternalHttpContext context = new InternalHttpContext(0, "", "", "", "", "", "", "");
        System.out.println(AbstractHttpContext.urlNeedsTenantIdHeader("/swagger-ui.html"));
        System.out.println(
            AbstractHttpContext.urlNeedsTenantIdHeader(
                "/webjars/springfox-swagger-ui/favicon-16x16.png?v=2.8.0-SNAPSHOT"
            )
        );

        String fileName = "test.json";
        LOGGER.info("Check content type " + Files.probeContentType(Paths.get(fileName)));
        fileName = "test.pdf";
        LOGGER.info("Check content type " + Files.probeContentType(Paths.get(fileName)));
        fileName = "test.xls";
        LOGGER.info("Check content type " + Files.probeContentType(Paths.get(fileName)));
    }
}
