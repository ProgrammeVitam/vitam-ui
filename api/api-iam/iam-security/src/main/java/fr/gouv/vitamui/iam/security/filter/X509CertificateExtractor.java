package fr.gouv.vitamui.iam.security.filter;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

@FunctionalInterface
public interface X509CertificateExtractor {
    X509Certificate extract(HttpServletRequest request);

    static X509CertificateExtractor requestAttributeX509CertificateExtractor() {
        return new RequestAttributeX509CertificateExtractor();
    }

    static X509CertificateExtractor requestHeaderX509CertificateExtractor(String certificateHeaderName) {
        RequestHeaderX509CertificateExtractor requestHeaderX509CertificateExtractor = new RequestHeaderX509CertificateExtractor();
        if (StringUtils.isNoneBlank(certificateHeaderName)) {
            requestHeaderX509CertificateExtractor.setCertificateHeaderName(certificateHeaderName);
        }
        return requestHeaderX509CertificateExtractor;
    }
}
