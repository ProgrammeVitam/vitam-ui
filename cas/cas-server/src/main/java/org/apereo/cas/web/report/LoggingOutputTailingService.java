package org.apereo.cas.web.report;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Copy/pasted from the CAS and cleaned from the log4j2 stuffs.
 *
 *
 */
public class LoggingOutputTailingService extends TailerListenerAdapter {

    public LoggingOutputTailingService(final TaskExecutor taskExecutor, final SimpMessagingTemplate stompMessagingTemplate, final Environment environment, final ResourceLoader resourceLoader) {
    }
}
