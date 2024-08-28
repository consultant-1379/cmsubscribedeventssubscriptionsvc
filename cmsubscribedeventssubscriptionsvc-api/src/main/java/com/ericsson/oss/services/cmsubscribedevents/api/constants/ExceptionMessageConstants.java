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

package com.ericsson.oss.services.cmsubscribedevents.api.constants;

/**
 * Contains Exception message text.
 */
public final class ExceptionMessageConstants {

    public static final String HEARTBEAT_GENERAL_ERROR = "Unable to establish heartbeat connection.";
    public static final String HEARTBEAT_TIMEOUT_ERROR = "Unable to establish heartbeat connection due to timeout.";
    public static final String HEARTBEAT_INVALID_URL_ERROR = "Failed to create subscription. Invalid URL for notificationRecipientAddress.";
    public static final String PUSH_VES_GENERAL_ERROR = "Failed to push VES event to notification recipient address.";

    private ExceptionMessageConstants(){
        throw new IllegalStateException("Utility class");
    }

}