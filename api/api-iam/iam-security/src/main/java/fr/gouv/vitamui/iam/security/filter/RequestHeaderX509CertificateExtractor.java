package fr.gouv.vitamui.iam.security.filter;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class RequestHeaderX509CertificateExtractor implements X509CertificateExtractor {

    private static final Log LOGGER = LogFactory.getLog(RequestHeaderX509CertificateExtractor.class);

    @Setter
    private String certificateHeaderName = "x-ssl-cert";

    @Override
    public X509Certificate extract(final HttpServletRequest request) {
        LOGGER.debug("Get X509 certificate from header " + certificateHeaderName);

        final String certificate = request.getHeader(certificateHeaderName);
        if (StringUtils.isBlank(certificate)) {
            LOGGER.error(String.format("Can not extract X509 certificate from header %s : with error: [Header value is empty!].", certificateHeaderName));
            return null;
        }

        try {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));
        } catch (final CertificateException e) {
            LOGGER.error(String.format("Can not extract X509 certificate from header %s : %s", certificateHeaderName, certificate), e);
        }

        return null;
    }

}
