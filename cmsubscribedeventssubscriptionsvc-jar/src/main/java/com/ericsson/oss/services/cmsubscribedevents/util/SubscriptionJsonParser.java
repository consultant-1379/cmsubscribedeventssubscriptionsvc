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

package com.ericsson.oss.services.cmsubscribedevents.util;

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.NOTIFICATION_RECIPIENT_ADDRESS;

import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Contains functions for parsing subscription JSON.
 */
public class SubscriptionJsonParser {

    @Inject
    Logger logger;

    /**
     * Get subscription notification recipient address from subscription.
     *
     * @param subscriptionJsonString
     *     - JSON string of subscription.
     * @return notificationRecipientAddress
     */
    public String getNotificationReceiptAddressValue(final String subscriptionJsonString) {
        String keyValue;
        try {
            final JsonNode jsonNode = new ObjectMapper().readTree(subscriptionJsonString);
            keyValue = jsonNode.path(HeartbeatValidator.NTF_SUBSCRIPTION_CONTROL).path(NOTIFICATION_RECIPIENT_ADDRESS).asText();
            if (keyValue.isEmpty()) {
                throw new IOException();
            }
            logger.debug("Retrieved Value {} for Key {} for path {}", keyValue, NOTIFICATION_RECIPIENT_ADDRESS,
                HeartbeatValidator.NTF_SUBSCRIPTION_CONTROL);
        } catch (IOException e) {
            logger.error("Unable to retrieve {} Key : {} value from subscription :{}", HeartbeatValidator.NTF_SUBSCRIPTION_CONTROL,
                NOTIFICATION_RECIPIENT_ADDRESS, subscriptionJsonString, e);
            throw new IllegalArgumentException();
        }
        return keyValue;
    }

}
