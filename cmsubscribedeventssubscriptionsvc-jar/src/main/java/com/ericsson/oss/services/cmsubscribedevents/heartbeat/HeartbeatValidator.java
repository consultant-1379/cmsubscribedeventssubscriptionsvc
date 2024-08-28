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
package com.ericsson.oss.services.cmsubscribedevents.heartbeat;

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_INVALID_URL_ERROR;

import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.builder.VesEventBuilder;
import com.ericsson.oss.services.cmsubscribedevents.model.values.NotificationEventTypeValuesMoiHeartbeat;
import com.ericsson.oss.services.cmsubscribedevents.model.ves.EventWrapper;
import com.ericsson.oss.services.cmsubscribedevents.nbi.HeartbeatNotificationSender;
import com.ericsson.oss.services.cmsubscribedevents.util.EventListenerUrlValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Validates the notificationRecipientAddress contains a valid EventListener URL by sending a VES heartbeat event to the inputted URL.
 */
public class HeartbeatValidator {

    public static final String NTF_SUBSCRIPTION_CONTROL = "ntfSubscriptionControl";

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatValidator.class);

    @Inject
    VesEventBuilder vesEventBuilder;

    @Inject
    HeartbeatNotificationSender heartbeatNotificationSender;

    @Inject
    EventListenerUrlValidator eventListenerUrlValidator;

    @Inject
    NotificationEventTypeValuesMoiHeartbeat notificationEventTypeValuesMoiHeartbeat;

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    /**
     * Checks NotificationRecipientAddress is a valid VES Event Listener URL that accepts VES Heartbeat Events.
     *
     * @param subscriptionNotificationRecipientAddress
     *     - Notification recipient address from subscription.
     * @return true if inputted subscription contains a notificationRecipientAddress of a URL that returns a 204 response to a VES heartbeat request.
     * @throws IOException
     *     - thrown when heartbeatNotificationBody has invalid content.
     */
    public boolean isSubscriptionHeartbeatUrlValid(final String subscriptionNotificationRecipientAddress) throws IOException {
        try {
            eventListenerUrlValidator.validateEventListenerUrl(subscriptionNotificationRecipientAddress);
        } catch (MalformedURLException | IllegalArgumentException e) {
            logger.debug("Failed to validate heartbeat URL {}", subscriptionNotificationRecipientAddress, e);
            throw new SubscriptionHeartbeatException(HEARTBEAT_INVALID_URL_ERROR);
        }
        logger.debug("Executing heartbeat check for {}", subscriptionNotificationRecipientAddress);
        final boolean isHearbeatSuccessful = executeHeartbeatRequest(subscriptionNotificationRecipientAddress);

        if (isHearbeatSuccessful) {
            eventsInstrumentationBean.incrementTotalSuccessfulHeartbeatRequests();
        } else {
            eventsInstrumentationBean.incrementTotalFailedHeartbeatRequests();
        }

        return isHearbeatSuccessful;
    }

    public boolean executeHeartbeatRequest(final String subscriptionClientUrl) throws IOException {
        final EventWrapper eventWrapper = vesEventBuilder.createEvent(notificationEventTypeValuesMoiHeartbeat);
        final String heartbeatNotificationJsonBody = new ObjectMapper().writeValueAsString(eventWrapper);
        logger.debug("Heartbeat notification body {} for URL {}", heartbeatNotificationJsonBody, subscriptionClientUrl);
        return heartbeatNotificationSender.isHeartbeatNotificationSuccessful(subscriptionClientUrl, new StringEntity(heartbeatNotificationJsonBody), 10);
    }

}
