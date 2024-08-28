/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.model.ves;

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.VES_EVENT_TIMESTAMP_FORMAT;

import com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatIntervalChangeListener;
import com.ericsson.oss.services.cmsubscribedevents.util.Time;
import javax.inject.Inject;
import java.util.UUID;

/**
 * Contains and sets attributes specific to Data object for Heartbeat NotificationType
 */
public class NotifyMoiDataHeartbeat extends CommonNotifyMoiData {

    @Inject
    HeartbeatIntervalChangeListener heartbeatIntervalChangeListener;

    /**
     * Creates an instance of a Heartbeat data object with default values.
     */
    public NotifyMoiDataHeartbeat() {
        this.href = System.getProperty(SubscriptionConstants.PRESENTATION_SERVER_NAME);
        final UUID uuid = UUID.randomUUID();
        this.notificationId = uuid.getMostSignificantBits() & Long.MAX_VALUE;
        this.eventTime = Time.getSystemTime(VES_EVENT_TIMESTAMP_FORMAT);
        this.systemDN = System.getProperty(SubscriptionConstants.PRESENTATION_SERVER_NAME);
        this.notificationType = "Heartbeat";
    }

    public int getHeartbeatNtfPeriod() {
        return heartbeatIntervalChangeListener.getCmSubscribedEventsHeartbeatInterval();
    }

}

