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
package com.ericsson.oss.services.cmsubscribedevents.ejb;

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_GENERAL_ERROR;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionPersistenceService;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException;
import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToDeleteException;
import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToGetException;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatIntervalChangeListener;
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatValidator;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean;
import com.ericsson.oss.services.cmsubscribedevents.util.SubscriptionJsonParser;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Implementation for Subscription service which provides subscription related processing.
 */
@Stateless
public class SubscriptionServiceBean implements SubscriptionService {

    @EServiceRef
    SubscriptionPersistenceService subscriptionPersistenceService;

    @Inject
    HeartbeatValidator heartbeatValidator;

    @Inject
    HeartbeatIntervalChangeListener heartbeatIntervalChangeListener;

    @Inject
    SubscriptionJsonParser subscriptionJsonParser;

    @Inject
    SubscriptionDbChangedMessageSender  subscriptionDbChangedMessageSender;

    @Inject
    private EventsInstrumentationBean eventsInstrumentationBean;

    /**
     * Gets the NotificationReceiptAddress from the inputted JSON String. Executes heartbeat validation if the CmSubscribedEventsHeartbeatInterval is
     * not 0. Passes the subscription to be created to the subscription manager.
     *
     * @param subscriptionJsonString
     *     JSON representation of the Subscription to be persisted.
     * @return Created Subscription in JSON with updated generated Subscription ID.
     * @throws IOException
     *     Exception if there is an issue creating the subscription.
     */
    @Override
    public String createSubscription(final String subscriptionJsonString) throws IOException {

        final String subscriptionNotificationRecipientAddress = subscriptionJsonParser.getNotificationReceiptAddressValue(
            subscriptionJsonString);
        if ((heartbeatIntervalChangeListener.getCmSubscribedEventsHeartbeatInterval() != 0) && (!heartbeatValidator.isSubscriptionHeartbeatUrlValid(
            subscriptionNotificationRecipientAddress))) {
            eventsInstrumentationBean.incrementTotalFailedHeartbeatRequests();
            throw new SubscriptionHeartbeatException(HEARTBEAT_GENERAL_ERROR);

        }
        final String subscription = subscriptionPersistenceService.createSubscription(subscriptionJsonString);
        subscriptionDbChangedMessageSender.sendSignalMessage();
        return subscription;
    }

    /**
     * Passes Subscription view all request to the subscription manager.
     *
     * @return String of the requested subscriptions in JSON format.
     * @throws IOException
     *     Throws an IO Exception.
     */
    @Override
    public String viewAllSubscriptions() throws IOException {
        return subscriptionPersistenceService.viewAllSubscriptions();
    }

    /**
     * Passes Subscription view request to subscription manager.
     *
     * @param subscriptionID
     *     Subscription subscriptionID.
     * @return Requested Subscription in JSON.
     * @throws IOException
     *     Throws an IO Exception.
     * @throws SubscriptionNotFoundToGetException
     *     Exception thrown if a subscription is not found.
     */
    @Override
    public String viewSubscription(final Integer subscriptionID) throws IOException, SubscriptionNotFoundToGetException {
        return subscriptionPersistenceService.viewSubscription(subscriptionID);
    }

    /**
     * Deletes subscription with matching subscription ID.
     *
     * @param subscriptionId
     *     The Subscription ID.
     * @throws SubscriptionNotFoundToDeleteException
     *     Exception thrown if a subscription is not found.
     */
    @Override
    public void deleteSubscription(final Integer subscriptionId) throws SubscriptionNotFoundToDeleteException {
        subscriptionPersistenceService.deleteSubscription(subscriptionId);
        subscriptionDbChangedMessageSender.sendSignalMessage();
    }

}