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
package com.ericsson.oss.services.cmsubscribedevents.api;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToDeleteException;
import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToGetException;
import java.io.IOException;
import javax.ejb.Remote;

/**
 * Subscription service which provides subscription related processing
 */
@EService
@Remote
public interface SubscriptionService {

    /**
     * Passes Subscription to be created to the subscription manager.
     *
     * @param subscriptionJsonString
     *     JSON representation of the Subscription to be persisted.
     * @return String of the created subscription in JSON format.
     * @throws IOException
     *     Throws an IO Exception.
     */
    String createSubscription(final String subscriptionJsonString) throws IOException;

    /**
     * Passes Subscription view all request to the subscription manager.
     *
     * @return String of the requested subscriptions in JSON format.
     * @throws IOException
     *     Throws an IO Exception.
     */
    String viewAllSubscriptions() throws IOException;

    /**
     * Passes Subscription view request to subscription manager.
     *
     * @param subscriptionID
     *     Subscription subscriptionID.
     * @return String of the requested subscription in JSON format.
     * @throws IOException
     *     Throws an IO Exception.
     * @throws SubscriptionNotFoundToGetException
     *     Exception thrown if a subscription is not found.
     */
    String viewSubscription(final Integer subscriptionID) throws IOException, SubscriptionNotFoundToGetException;

    /**
     * Passes Subscription delete request to subscription manager.
     *
     * @param subscriptionID
     *     Subscription subscriptionID.
     * @throws SubscriptionNotFoundToDeleteException
     *     Exception thrown if a subscription is not found.
     */
    void deleteSubscription(final Integer subscriptionID) throws SubscriptionNotFoundToDeleteException;

}