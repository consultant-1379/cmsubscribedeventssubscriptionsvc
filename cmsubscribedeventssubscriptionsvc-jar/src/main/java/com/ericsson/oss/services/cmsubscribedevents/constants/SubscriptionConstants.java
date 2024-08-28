/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.constants;

/**
 * Subscriptions related constants.
 */
public final class SubscriptionConstants {

    public static final String NOTIFICATION_RECIPIENT_ADDRESS = "notificationRecipientAddress";

    public static final String PRESENTATION_SERVER_NAME = "presentationServerName";

    public static final String VES_EVENT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private SubscriptionConstants() {
        throw new IllegalStateException("Utility class");
    }

}