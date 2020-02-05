package fr.gouv.vitamui.commons.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.AbstractHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;

public class TestUri {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TestUri.class);

    public static void main(final String[] args) throws IOException {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        builder.queryParam("test", OffsetDateTime.now().toString());

        System.out.println();
        System.out.println("Builder toString " + builder.build().toUri());
        System.out.println("Builder toString toUriString : " + builder.build().toUriString());
        System.out.println("Builder toString Encode toUriString : " + builder.build().encode().toUriString());

        System.out.println();
        final URIBuilder uri = new URIBuilder().setScheme("http").setHost("www.google.com");
        uri.addParameter("test", OffsetDateTime.now().toString());
        try {
            System.out.println("Apache Builder toString " + uri.build().getRawQuery());
        }
        catch (final URISyntaxException e) {
            LOGGER.error("Error Building URL : ", e);
        }

        final InternalHttpContext context = new InternalHttpContext(0, "", "", "", "", "", "", "");
        System.out.println(AbstractHttpContext.urlNeedsTenantIdHeader("/swagger-ui.html"));
        System.out.println(AbstractHttpContext.urlNeedsTenantIdHeader("/webjars/springfox-swagger-ui/favicon-16x16.png?v=2.8.0-SNAPSHOT"));

        String fileName = "test.json";
        System.out.println("Check content type " + Files.probeContentType(Paths.get(fileName)));
        fileName = "test.pdf";
        System.out.println("Check content type " + Files.probeContentType(Paths.get(fileName)));
        fileName = "test.xls";
        System.out.println("Check content type " + Files.probeContentType(Paths.get(fileName)));
    }
}
