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

import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToGetException;
import com.ericsson.oss.services.cmsubscribedevents.exceptions.SubscriptionNotFoundToDeleteException;

import java.io.IOException;

public class SubscriptionServiceImpl implements SubscriptionService{

    public static String viewSubscriptions = "[{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"http://localhost:8080/eventListener/v1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"1\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}]";

    @Override
    public String createSubscription(String subscriptionJsonString)
            throws IOException {
        return null;
    }

    @Override
    public String viewAllSubscriptions() throws IOException {
        return viewSubscriptions;
    }

    public static void setViewSubscription(final String viewSubscriptions) {
        SubscriptionServiceImpl.viewSubscriptions = viewSubscriptions;
    }
    @Override
    public String viewSubscription(Integer subscriptionID) throws IOException,
            SubscriptionNotFoundToGetException {
        return null;
    }

    @Override
    public void deleteSubscription(Integer subscriptionID)
            throws SubscriptionNotFoundToDeleteException {
    }

}
