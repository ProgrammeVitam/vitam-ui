package fr.gouv.vitamui.cas.pm;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.web.support.WebUtils;

/**
 * Specific expiration policy builder for password management.
 */
public class PmTransientSessionTicketExpirationPolicyBuilder extends TransientSessionTicketExpirationPolicyBuilder {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PmTransientSessionTicketExpirationPolicyBuilder.class);

    public static final String PM_EXPIRATION_IN_MINUTES_ATTRIBUTE = "pmExpirationInMinutes";

    public PmTransientSessionTicketExpirationPolicyBuilder(final CasConfigurationProperties casProperties) {
        super(casProperties);
    }

    @Override
    public ExpirationPolicy toTransientSessionTicketExpirationPolicy() {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        if (request != null) {
            val expInMinutesAttribute = request.getAttribute(PM_EXPIRATION_IN_MINUTES_ATTRIBUTE);
            if (expInMinutesAttribute != null) {
                try {
                    val expInMinutes = Integer.parseInt((String) expInMinutesAttribute);
                    return new HardTimeoutExpirationPolicy(expInMinutes * 60);
                } catch (final NumberFormatException e) {
                    LOGGER.error("Cannot get expiration in minutes", e);
                }
            }
        }
        return new HardTimeoutExpirationPolicy(casProperties.getAuthn().getPm().getReset().getExpirationMinutes() * 60);
    }
}
