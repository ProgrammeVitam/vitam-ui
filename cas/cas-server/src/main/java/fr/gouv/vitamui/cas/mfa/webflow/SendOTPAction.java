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
package fr.gouv.vitamui.cas.mfa.webflow;

import fr.gouv.vitamui.cas.mfa.util.CodeStringGenerator;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Date;

/**
 * The webflow action to send SMS code.
 *
 *
 */
public class SendOTPAction extends AbstractAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SendOTPAction.class);

    private static final String CODE_TEXT_KEY = "cas.mfa.sms.code.text";

    private final RandomStringGenerator codeGenerator = new CodeStringGenerator();

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Value("${mfa.sms.sender}")
    private String smsSender;

    @Autowired
    @Qualifier("messageSource")
    private HierarchicalMessageSource messageSource;

    @Autowired
    private Utils utils;

    @Override
    protected Event doExecute(final RequestContext context) {

        final Authentication authentication = WebUtils.getAuthentication(context);
        LOGGER.debug("authentication: {}", authentication);
        final UserDto user = utils.getRealUser(authentication);
        LOGGER.debug("user: {}", user);
        final String mobile = user.getMobile();
        LOGGER.debug("mobile: {}", mobile);
        final MutableAttributeMap<Object> flowScope = context.getFlowScope();

        if (StringUtils.isNotBlank(mobile)) {
            final String code = codeGenerator.getNewString();
            flowScope.put(Constants.GENERATED_CODE, code);
            flowScope.put(Constants.GENERATION_DATE, new Date());
            LOGGER.debug("Generating code: {}", code);

            final String message = messageSource.getMessage(CODE_TEXT_KEY, new Object[] { code }, LocaleContextHolder.getLocale());
            communicationsManager.sms(smsSender, mobile, message);

            flowScope.put("mobile", obfuscateMobile(mobile));
            return success();
        } else {
            final String firstName = user.getFirstname();
            flowScope.put("firstname", firstName);
            return error();
        }
    }

    private String obfuscateMobile(final String mobile) {
        String m = mobile.replaceFirst("\\+33", "0");
        m = m.substring(0, 2) + " XX XX XX " + m.substring(m.length() - 2);
        return m;
    }
}
