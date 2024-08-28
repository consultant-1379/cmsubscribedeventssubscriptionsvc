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

package com.ericsson.oss.services.cmsubscribedevents.model.values;

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.PRESENTATION_SERVER_NAME;

import com.ericsson.oss.services.cmsubscribedevents.model.ves.CommonNotifyMoiData;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.NotifyMoiDataHeartbeat;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Values specific to MOI Heartbeat. Used for HeartbeatNotificationEvent JSON body creation using the VesEventBuilder.
 */
public class NotificationEventTypeValuesMoiHeartbeat implements CommonNotificationEventTypeValues {

    private static final String EVENT_NAME = "Heartbeat_ENM-Ericsson_VES";
    private static final String DOMAIN = "heartbeat";
    private static final String NAMESPACE = "3GPP-Heartbeat";

    @Inject
    Instance<NotifyMoiDataHeartbeat> notifyMoiDataHeartbeatInstance;

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getStndDefinedNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getReportingEntityName() {
        return System.getProperty(PRESENTATION_SERVER_NAME);
    }

    @Override
    public String getEventIdPrefix() {
        return "Heartbeat_";
    }

    @Override
    public String getSourceName() {
        return System.getProperty(PRESENTATION_SERVER_NAME);
    }

    @Override
    public Long getSequenceNumber() {
        return 0L;
    }

    @Override
    public CommonNotifyMoiData getNotifyMoiData() {
        return notifyMoiDataHeartbeatInstance.select().get();
    }

}
