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

import com.ericsson.oss.services.cmsubscribedevents.model.values.CommonNotificationEventTypeValues;

/**
 * Holds the VES StndDefinedFields information.
 */
public class StndDefinedFields {

    private final String stndDefinedFieldsVersion;

    private final CommonNotifyMoiData data;

    /**
     * Creates StndDefinedFields for a VES event.
     *
     * @param commonNotificationEventTypeValues
     *     - Values for specific Notification Event Types.
     */
    public StndDefinedFields(final CommonNotificationEventTypeValues commonNotificationEventTypeValues) {
        this.stndDefinedFieldsVersion = "1.0";
        this.data = commonNotificationEventTypeValues.getNotifyMoiData();
    }

    public String getStndDefinedFieldsVersion() {
        return stndDefinedFieldsVersion;
    }

    public CommonNotifyMoiData getData() {
        return data;
    }

}