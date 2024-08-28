/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.cmsubscribedevents.nbi.queue;

/**
 * Interface to be implemented by those sending events from the EventQueue
 *
 */
public interface EventQueueSender {

    /**
     * Action to be taken to start sending events on the EventQueue
     */
    void startSending();
}
