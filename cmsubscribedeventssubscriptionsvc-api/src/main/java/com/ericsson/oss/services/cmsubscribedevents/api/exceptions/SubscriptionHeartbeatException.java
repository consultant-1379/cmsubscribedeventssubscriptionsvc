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
package com.ericsson.oss.services.cmsubscribedevents.api.exceptions;

import javax.ejb.ApplicationException;

/**
 * Application exception for heartbeat failure.
 */
@ApplicationException(rollback = true)
public class SubscriptionHeartbeatException extends RuntimeException {

    private static final long serialVersionUID = 6834200866363809458L;

    public SubscriptionHeartbeatException(final String message) {
        super(message);
    }

}
