/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.cas.x509;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

/**
 * Extracteur de certificat personnalisé depuis la requête.
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
            LOGGER.warn("Client certificate is mandatory but not found in request headers.");
            return null;
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

    // Nginx transmet le PEM encodé en URL, avec des tabulations pour les sauts de ligne et les '+' transformés en espaces.
    protected X509Certificate parseCertificateGeneratedByNginx(final String header) throws CertificateException {
        final String decoded = URLDecoder.decode(header.replace("\t", "\n"), StandardCharsets.UTF_8);
        return toCertificate(stripMarkers(decoded).replace(" ", "+"));
    }

    // Apache transmet le PEM avec de simples espaces représentant les sauts de ligne.
    protected X509Certificate parseCertificateGeneratedByApache(final String header) throws CertificateException {
        return toCertificate(stripMarkers(header).replace(" ", ""));
    }

    private static String stripMarkers(final String pem) {
        return pem.replace(BEGIN_CERT, "").replace(END_CERT, "");
    }

    private static X509Certificate toCertificate(final String base64Body) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(base64Body.replace("\n", "")))
        );
    }
}
