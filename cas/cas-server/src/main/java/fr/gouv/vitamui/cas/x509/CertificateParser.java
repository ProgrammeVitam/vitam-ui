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

import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.cryptacular.x509.GeneralNameType;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Certificate parser
 */
public class CertificateParser {

    private CertificateParser() {}

    public static String extract(final X509Certificate cert, final X509AttributeMapping mapping)
        throws CertificateParsingException {
        val name = mapping.getName();
        String value = null;
        if (X509CertificateAttributes.ISSUER_DN.name().equalsIgnoreCase(name)) {
            value = cert.getIssuerDN().getName();
        } else if (X509CertificateAttributes.SUBJECT_DN.name().equalsIgnoreCase(name)) {
            value = cert.getSubjectDN().getName();
        } else if (X509CertificateAttributes.SUBJECT_ALTERNATE_NAME.name().equalsIgnoreCase(name)) {
            val altNames = cert.getSubjectAlternativeNames();
            final StringBuilder subjectAltNamesBuilder = new StringBuilder();
            if (altNames != null && altNames.size() > 0) {
                for (final var attribute : altNames) {
                    if (Objects.nonNull(attribute) && attribute.size() == 2) {
                        final int attributeKey = (int) attribute.get(0);
                        final String attributeName = GeneralNameType.fromTagNumber(attributeKey).name();
                        final var attributeValue = attribute.get(1);
                        subjectAltNamesBuilder.append(attributeName).append("=").append(attributeValue).append(", ");
                    }
                }
                value = subjectAltNamesBuilder.length() > 0
                    ? subjectAltNamesBuilder.substring(0, subjectAltNamesBuilder.length() - 2)
                    : "";
            }
        }
        if (value == null) {
            throw new CertificateParsingException("Cannot find X509 value for: " + name);
        }
        val parsing = mapping.getParsing();
        val expansion = mapping.getExpansion();
        if (StringUtils.isNotBlank(parsing)) {
            val pattern = Pattern.compile(parsing);
            val matcher = pattern.matcher(value);
            if (matcher.matches()) {
                val groupCount = matcher.groupCount();
                if (groupCount == 0) {
                    throw new CertificateParsingException("Parsing fails for X509 value: " + value);
                }
                if (StringUtils.isBlank(expansion)) {
                    value = matcher.group(1);
                } else {
                    value = expansion;
                    for (int i = 0; i < groupCount; i++) {
                        value = value.replace("{" + i + "}", matcher.group(i + 1));
                    }
                }
            }
        }
        return value;
    }
}
