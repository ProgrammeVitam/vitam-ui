package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;

/**
 * Copy/pasted from the original CAS class and emptied (because of log4j2).
 *
 *
 */
public class LoggingConfigController extends BaseCasMvcEndpoint {

    public LoggingConfigController(final AuditTrailExecutionPlan auditTrailManager, final CasConfigurationProperties casProperties) {
        super("casloggingconfig", "/logging", casProperties.getMonitor().getEndpoints().getLoggingConfig(), casProperties);
    }
}
