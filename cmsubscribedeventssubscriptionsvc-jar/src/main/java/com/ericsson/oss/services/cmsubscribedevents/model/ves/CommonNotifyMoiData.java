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

/**
 * Abstract implementation of NotifyMOI data containing attributes common to all NotifyMoiTypes.
 */
public abstract class CommonNotifyMoiData {

    String href;

    long notificationId;

    String notificationType;

    String eventTime;

    String systemDN;

    public String getHref() {
        return href;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getSystemDN() {
        return systemDN;
    }

}