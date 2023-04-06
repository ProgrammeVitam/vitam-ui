package fr.gouv.vitamui.iam.security.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

public class RequestAttributeX509CertificateExtractor implements X509CertificateExtractor {

    public static final String REQUEST_X509_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    private static final Log LOGGER = LogFactory.getLog(RequestAttributeX509CertificateExtractor.class);

    @Override
    public X509Certificate extract(final HttpServletRequest request) {
        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(REQUEST_X509_CERTIFICATE_ATTRIBUTE);
        if (certificates != null && certificates.length > 0) {
            LOGGER.debug("X.509 client authentication certificate:" + certificates[0]);
            return certificates[0];
        }
        LOGGER.debug("No client certificate found in request.");
        return null;
    }
}
