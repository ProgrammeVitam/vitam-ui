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
package fr.gouv.vitamui.cas.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Helper class.
 *
 *
 */
@RequiredArgsConstructor
public class Utils {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(Utils.class);

    private static final int BROWSER_SESSION_LIFETIME = -1;

    private final String casToken;

    private final Integer casTenantIdentifier;

    private final String casIdentity;

    private final JavaMailSender mailSender;

    public ExternalHttpContext buildContext(final String username) {
        return new ExternalHttpContext(casTenantIdentifier, casToken, "cas+" + username, casIdentity);
    }

    public Event performClientRedirection(final Action action, final SAML2Client client, final RequestContext requestContext) throws IOException {
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val service = WebUtils.getService(requestContext);

        String url = CommonHelper.addParameter("clientredirect", Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, client.getName());
        if (service != null) {
            url = CommonHelper.addParameter(url, CasProtocolConstants.PARAMETER_SERVICE, service.getOriginalUrl());
        }
        response.sendRedirect(url);

        final ExternalContext externalContext = requestContext.getExternalContext();
        externalContext.recordResponseComplete();
        return new Event(action, CasWebflowConstants.TRANSITION_ID_STOP);
    }

    public Cookie buildIdpCookie(final String value, final TicketGrantingCookieProperties tgc) {
        final Cookie cookie = new Cookie(CommonConstants.IDP_PARAMETER, value);
        cookie.setPath(tgc.getPath());
        cookie.setDomain(tgc.getDomain());
        cookie.setMaxAge(BROWSER_SESSION_LIFETIME);
        cookie.setSecure(tgc.isSecure());
        cookie.setHttpOnly(tgc.isHttpOnly());
        return cookie;
    }

    public Object getAttributeValue(final Map<String, List<Object>> attributes, final String key) {
        final List<Object> attributeList = attributes.get(key);
        if (CollectionUtils.isNotEmpty(attributeList)) {
            return attributeList.get(0);
        }
        return null;
    }

    public String sanitizePasswordResetUrl(final String url) {
        if (url != null && url.length() > 15) {
            return url.substring(0, url.length() - 15) + "...";
        }
        else {
            return "\"passwordResetURL\"...";
        }
    }

    public boolean htmlEmail(final String text, final String from, final String subject, final String to, final String cc, final String bcc) {
        try {
            if (mailSender == null || StringUtils.isBlank(text) || StringUtils.isBlank(from)
                || StringUtils.isBlank(subject) || StringUtils.isBlank(to)) {
                LOGGER.warn(
                    "Could not send email to [{}] because either no address/subject/text is found or email settings are not configured.",
                    to);
                return false;
            }

            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            message.setContent(text, "text/html; charset=UTF-8");
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setPriority(1);

            if (org.apache.commons.lang3.StringUtils.isNotBlank(cc)) {
                helper.setCc(cc);
            }

            if (org.apache.commons.lang3.StringUtils.isNotBlank(bcc)) {
                helper.setBcc(bcc);
            }
            mailSender.send(message);
            return true;
        }
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return false;
    }

    public String getIdpValue(final HttpServletRequest request) {
        String idp = request.getParameter(CommonConstants.IDP_PARAMETER);
        if (StringUtils.isNotBlank(idp)) {
            return idp;
        }
        val cookie = org.springframework.web.util.WebUtils.getCookie(request, CommonConstants.IDP_PARAMETER);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}
