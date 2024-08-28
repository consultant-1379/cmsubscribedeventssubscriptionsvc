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

package com.ericsson.oss.services.cmsubscribedevents.model.ves;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 *  Holds common node notification data shared across the COM ECIM, CPP, and DPS notifications.
 */
public class CommonNotificationData {

    private static final Logger logger = LoggerFactory.getLogger(CommonNotificationData.class);

    private final String Dn;

    private final Long notifSqNr;

    private final Map<String, Object> updateAttributes;

    private final String timestamp;

    /**
     * Creates the CommonNotificationData.
     * @param Dn
     *      - The Distinguished Name of object responsible for sending the notification.
     * @param timestamp
     *      - The time the notification was sent.
     * @param notifSqNr
     *      - The notification sequence number.
     * @param updatedAttributes
     *      - The updated attributes of the node.
     */
    public CommonNotificationData(final String Dn, final String timestamp, final Long notifSqNr, final Map<String, Object> updatedAttributes) {
        this.Dn = Dn;
        this.timestamp = timestamp;
        this.notifSqNr = notifSqNr;
        this.updateAttributes = Collections.unmodifiableMap(updatedAttributes);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName() + " fields are : \n");
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field field : fields) {
            try {
                result.append(field.getName());
                result.append(": ");
                result.append(field.get(this));
            } catch (final IllegalAccessException exception) {
                logger.error("Exception while preparing {} toString() - Exception {}", this.getClass().getName(), exception.getMessage());
            }
            result.append(", ");
        }
        return result.toString();
    }

}