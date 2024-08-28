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

package com.ericsson.oss.services.cmsubscribedevents.enums;

/**
 * Enum representing the event's operation type.
 */
public enum OperationType {

    CREATE("notifyMOICreation"),
    DELETE("notifyMOIDeletion"),
    REPLACE("notifyMOIAttributeValueChanges"),
    ALL_CHANGES("notifyMOIChanges");

    private final String notificationType;

    OperationType(final String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationType() {
        return notificationType;
    }

}