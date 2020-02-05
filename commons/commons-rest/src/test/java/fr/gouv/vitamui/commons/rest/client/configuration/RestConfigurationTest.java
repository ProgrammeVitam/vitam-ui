package fr.gouv.vitamui.commons.rest.client.configuration;

import fr.gouv.vitamui.commons.rest.RestTestApplicationConfiguration;
import fr.gouv.vitamui.commons.rest.StartRestTestApplication;
import fr.gouv.vitamui.commons.rest.util.AbstractServerIdentityBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ListIterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Rest Client Configuration.
 *
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = StartRestTestApplication.class)
public class RestConfigurationTest extends AbstractServerIdentityBuilder {

    @Autowired
    RestTestApplicationConfiguration applicationConfiguration;

    @Autowired
    RestClientConfiguration restClientConfiguration1;

    @Autowired
    RestClientConfiguration restClientConfiguration2;

    @Autowired
    HttpPoolConfiguration httpPoolConfiguration;

    @Test
    public void testRestConfiguration() {
        assertTrue(restClientConfiguration1 == applicationConfiguration.getRestClientConfiguration1());
        assertTrue(restClientConfiguration2 == applicationConfiguration.getRestClientConfiguration2());
    }

    @Test
    public void testRestConfiguration1() {
        assertTrue(restClientConfiguration1.getServerHost().equals("restapp1.service.consul"));
        assertTrue(restClientConfiguration1.getServerPort() == 12345678);
        assertTrue(restClientConfiguration1.isSecure());
    }

    @Test
    public void testRestConfiguration2() {
        assertTrue(restClientConfiguration2.getServerHost().equals("restapp2.service.consul"));
        assertTrue(restClientConfiguration2.getServerPort() == 87654321);
        assertTrue(!restClientConfiguration2.isSecure());
    }

    @Test
    public void testSslConfiguration() {
        SSLConfiguration sslConfig = restClientConfiguration1.getSslConfiguration();
        assertNotNull(sslConfig);
        assertTrue(sslConfig.isHostnameVerification());

        SSLConfiguration.CertificateStoreConfiguration ks = sslConfig.getKeystore();
        assertNotNull(ks);
        assertTrue(ks.getKeyPath().endsWith("keystore_rest-app.p12"));
        assertTrue(ks.getKeyPassword().equals("azerty"));

        SSLConfiguration.CertificateStoreConfiguration ts = sslConfig.getTruststore();
        assertNotNull(ts);
        assertTrue(ts.getKeyPath().endsWith("truststore_rest-app.jks"));
        assertTrue(ts.getKeyPassword().equals("azerty"));
    }

    @Test
    public void testHttpHostConfiguration() {
        assertNotNull(httpPoolConfiguration);
        assertTrue(httpPoolConfiguration.getMaxTotal() == 500);
        assertTrue(httpPoolConfiguration.getMaxPerRoute() == 50);

        ListIterator<HttpPoolConfiguration.HostConfiguration> it = httpPoolConfiguration.getHostConfigurations().listIterator();
        HttpPoolConfiguration.HostConfiguration firstHttpHost = it.next();
        assertTrue(firstHttpHost.getScheme().equals("http"));
        assertTrue(firstHttpHost.getHost().equals("localhost"));
        assertTrue(firstHttpHost.getPort() == 12345678);
        assertTrue(firstHttpHost.getMaxPerRoute() == 20);

        HttpPoolConfiguration.HostConfiguration secondHttpHost = it.next();
        assertTrue(secondHttpHost.getScheme().equals("https"));
        assertTrue(secondHttpHost.getHost().equals("localhost"));
        assertTrue(secondHttpHost.getPort() == 87654321);
        assertTrue(secondHttpHost.getMaxPerRoute() == 10);
    }

}
