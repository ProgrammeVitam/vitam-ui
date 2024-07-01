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
package fr.gouv.vitamui.cas.x509;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

/**
 * Custom certificate extractor from the request.
 */
@Slf4j
public class CustomRequestHeaderX509CertificateExtractor implements X509CertificateExtractor {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private final String customCertificateHeader;

    private final boolean x509AuthnMandatory;

    public CustomRequestHeaderX509CertificateExtractor(
        final String customCertificateHeader,
        final boolean x509AuthnMandatory
    ) {
        this.customCertificateHeader = customCertificateHeader;
        this.x509AuthnMandatory = x509AuthnMandatory;
    }

    @Override
    public X509Certificate[] extract(final HttpServletRequest request) {
        final X509Certificate[] certs = internalExtract(request);
        if (x509AuthnMandatory && certs == null) {
            throw new RuntimeException("Client certificate is mandatory!");
        }
        return certs;
    }

    protected X509Certificate[] internalExtract(final HttpServletRequest request) {
        final String certHeader = request.getHeader(customCertificateHeader);
        if (StringUtils.isBlank(certHeader)) {
            LOGGER.debug("Certificates not found via custom header: {}", customCertificateHeader);
            return null;
        }

        X509Certificate cert = null;
        try {
            cert = parseCertificateGeneratedByNginx(certHeader);
        } catch (final Exception e) {
            LOGGER.debug("Nginx parsing exception: {}", e.getMessage());
            try {
                cert = parseCertificateGeneratedByApache(certHeader);
            } catch (final Exception e2) {
                LOGGER.debug("Apache parsing exception: {}", e2.getMessage());
            }
        }
        if (cert == null) {
            LOGGER.error("Cannot parse certificate from Apache and Nginx");
            return null;
        }

        final X509Certificate[] certificates = new X509Certificate[1];
        certificates[0] = cert;

        LOGGER.debug(
            "[{}] Certificate(s) found via custom header: [{}]",
            certificates.length,
            Arrays.toString(certificates)
        );
        return certificates;
    }

    protected X509Certificate parseCertificateGeneratedByNginx(final String header) throws CertificateException {
        final String data = header.replaceAll("\t", "\n");

        final String decoded = URLDecoder.decode(data, StandardCharsets.UTF_8);
        final String cert = decoded
            .replace(BEGIN_CERT, "")
            .replace(END_CERT, "")
            .replaceAll(" ", "+")
            .replaceAll("\\n", "");

        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(cert))
        );
    }

    protected X509Certificate parseCertificateGeneratedByApache(final String header) throws CertificateException {
        final String cert = header
            .replace(BEGIN_CERT, "")
            .replace(END_CERT, "")
            .replaceAll(" ", "")
            .replaceAll("\\n", "");

        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(cert))
        );
    }
}
