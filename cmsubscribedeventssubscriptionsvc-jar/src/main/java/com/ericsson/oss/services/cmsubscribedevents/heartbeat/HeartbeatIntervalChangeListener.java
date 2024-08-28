/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.cmsubscribedevents.heartbeat;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Listener for Configuration parameter (PIB) changes.
 */
@ApplicationScoped
public class HeartbeatIntervalChangeListener {

    private static final String CM_SUBSCRIBED_EVENTS_HEARTBEAT = "cmSubscribedEventsHeartbeatInterval";

    @Inject
    @Configured(propertyName = CM_SUBSCRIBED_EVENTS_HEARTBEAT)
    private int cmSubscribedEventsHeartbeatInterval;

    @Inject
    private Logger logger;

    void listenForCmSubscribedEventsHeartbeatIntervalChanges(
            @Observes @ConfigurationChangeNotification(propertyName = CM_SUBSCRIBED_EVENTS_HEARTBEAT) final int newCmSubscribedEventsHeartbeatInterval) {
        logger.info("{} parameter change listener invoked. New value for the parameter is: {}", CM_SUBSCRIBED_EVENTS_HEARTBEAT,
                newCmSubscribedEventsHeartbeatInterval);
        cmSubscribedEventsHeartbeatInterval = newCmSubscribedEventsHeartbeatInterval;
    }

    public int getCmSubscribedEventsHeartbeatInterval() {
        return cmSubscribedEventsHeartbeatInterval;
    }
}

