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
 *
 */

package com.ericsson.oss.services.cmsubscribedevents.model.ves;

/**
 * Container for VES Events content specified in 3GPP TS 28.532.
 */
public class Event {

    private final CommonEventHeader commonEventHeader;

    private final StndDefinedFields stndDefinedFields;

    /**
     * Creates a VES Event object.
     *
     * @param commonEventHeader
     *     - Holds the VES commonEventHeader information.
     * @param stndDefinedFields
     *     - Holds the VES stndDefinedFields information.
     */
    public Event(final CommonEventHeader commonEventHeader, final StndDefinedFields stndDefinedFields) {
        this.commonEventHeader = commonEventHeader;
        this.stndDefinedFields = stndDefinedFields;
    }

    public CommonEventHeader getCommonEventHeader() {
        return commonEventHeader;
    }

    public StndDefinedFields getStndDefinedFields() {
        return stndDefinedFields;
    }

}