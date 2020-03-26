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
package fr.gouv.vitamui.cas.webflow.actions;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Custom initial flow action to retrieve pre-filled inputs.
 *
 *
 */
public class CustomInitialFlowSetupAction extends InitialFlowSetupAction {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomInitialFlowSetupAction.class);

    private final String vitamuiPortalUrl;

    private final String surrogationSeperator;

    @Value("${theme.vitam-logo:#{null}}")
    private String vitamLogoPath;

    @Value("${theme.vitamui-logo-large:#{null}}")
    private String vitamuiLargeLogoPath;

    public CustomInitialFlowSetupAction(final List<ArgumentExtractor> argumentExtractors,
                                        final ServicesManager servicesManager,
                                        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                                        final CasCookieBuilder warnCookieGenerator,
                                        final CasConfigurationProperties casProperties,
                                        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                        final SingleSignOnParticipationStrategy renewalStrategy,
                                        final TicketRegistrySupport ticketRegistrySupport,
                                        final String vitamuiPortalUrl,
                                        final String surrogationSeperator) {
        super(argumentExtractors, servicesManager, authenticationRequestServiceSelectionStrategies, ticketGrantingTicketCookieGenerator,
            warnCookieGenerator, casProperties, authenticationEventExecutionPlan, renewalStrategy, ticketRegistrySupport);
        this.vitamuiPortalUrl = vitamuiPortalUrl;
        this.surrogationSeperator = surrogationSeperator;
    }

    @Override
    public Event doExecute(final RequestContext context) {

        final MutableAttributeMap<Object> flowScope = context.getFlowScope();
        flowScope.put(Constants.PORTAL_URL, vitamuiPortalUrl);

        String username = context.getRequestParameters().get(Constants.USERNAME);
        if (username != null) {

            username = username.toLowerCase();
            LOGGER.debug("Provided username: {}", username);
            if (username.startsWith(surrogationSeperator)) {
                username = StringUtils.substringAfter(username, surrogationSeperator);
            }

            WebUtils.putCredential(context, new UsernamePasswordCredential(username, null));

            if (username.contains(surrogationSeperator)) {
                final String[] parts = username.split("\\" + surrogationSeperator);
                flowScope.put(Constants.SURROGATE, parts[0]);
                flowScope.put(Constants.SUPER_USER, parts[1]);
            }
        }

        if(vitamLogoPath != null) {
            try {
                Path logoFile = Paths.get(vitamLogoPath);
                String logo = DatatypeConverter.printBase64Binary(Files.readAllBytes(logoFile));
                flowScope.put(Constants.VITAM_LOGO, logo);
            } catch (IOException e) {
                LOGGER.warn("Can't find vitam logo");
                e.printStackTrace();
            }
        }
        if(vitamuiLargeLogoPath != null) {
            try {
                Path logoFile = Paths.get(vitamuiLargeLogoPath);
                String logo = DatatypeConverter.printBase64Binary(Files.readAllBytes(logoFile));
                flowScope.put(Constants.VITAM_UI_LARGE_LOGO, logo);
            } catch (IOException e) {
                LOGGER.warn("Can't find vitam ui large logo");
                e.printStackTrace();
            }
        }

        return super.doExecute(context);
    }
}
