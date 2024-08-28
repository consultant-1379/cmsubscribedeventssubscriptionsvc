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
 * Wrapper class for Event Object.
 */
public class EventWrapper {

    private final Event event;

    /**
     * Creates an Event Wrapper for VES events.
     *
     * @param event
     *     - VES event.
     */
    public EventWrapper(final Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

}