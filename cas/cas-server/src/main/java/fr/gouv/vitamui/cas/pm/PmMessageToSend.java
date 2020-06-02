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
package fr.gouv.vitamui.cas.pm;

import java.util.Locale;

import org.springframework.context.HierarchicalMessageSource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Message to send in the context of the password management.
 *
 *
 */
@Getter
@ToString
@EqualsAndHashCode
public class PmMessageToSend {

    public static final String ONE_DAY = "1day";

    private static final String PM_RESET_SUBJECT_KEY = "cas.authn.pm.reset.subject";

    private static final String PM_RESET_TEXT_KEY = "cas.authn.pm.reset.text";

    private static final String PM_ACCOUNTCREATION_SUBJECT_KEY = "cas.authn.pm.accountcreation.subject";

    private static final String PM_ACCOUNTCREATION_TEXT_KEY = "cas.authn.pm.accountcreation.text";

    private final String subject;

    private final String text;

    private PmMessageToSend(final String subject, final String text) {
        this.subject = subject;
        this.text = text;
    }

    public static PmMessageToSend buildMessage(final HierarchicalMessageSource messageSource, final String firstname,
            final String lastname, final String ttlInMinutes, final String url, final String platformName, final Locale locale) {
        final String subject;
        final String text;
        if (ONE_DAY.equals(ttlInMinutes)) {
            subject = messageSource.getMessage(PM_ACCOUNTCREATION_SUBJECT_KEY, null, locale);
            text = messageSource.getMessage(PM_ACCOUNTCREATION_TEXT_KEY,
                    new Object[] { firstname, lastname, "24", url, platformName }, locale);
        } else {
            subject = messageSource.getMessage(PM_RESET_SUBJECT_KEY, null, locale);
            text = messageSource.getMessage(PM_RESET_TEXT_KEY, new Object[] { firstname, lastname, ttlInMinutes, url, platformName},
                    locale);
        }
        return new PmMessageToSend(subject, text);
    }
}
