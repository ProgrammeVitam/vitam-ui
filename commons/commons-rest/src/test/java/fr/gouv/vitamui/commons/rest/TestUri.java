package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.rest.client.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class TestUri {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUri.class);

    public static void main(final String[] args) throws IOException, URISyntaxException {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUri(new URI("http://localhost"));

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

        System.out.println(HttpContext.urlNeedsTenantIdHeader("/swagger-ui.html"));
        System.out.println(
            HttpContext.urlNeedsTenantIdHeader("/webjars/springfox-swagger-ui/favicon-16x16.png?v=2.8.0-SNAPSHOT")
        );

        String fileName = "test.json";
        LOGGER.info("Check content type " + Files.probeContentType(Path.of(fileName)));
        fileName = "test.pdf";
        LOGGER.info("Check content type " + Files.probeContentType(Path.of(fileName)));
        fileName = "test.xls";
        LOGGER.info("Check content type " + Files.probeContentType(Path.of(fileName)));
    }
}
