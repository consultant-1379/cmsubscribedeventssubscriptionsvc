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

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.SignalMessageConstants.SUBSCRIPTION_UPDATE;

import com.ericsson.oss.itpf.sdk.cluster.classic.ClusterMessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

public class SubscriptionDbChangedMessageListener implements ClusterMessageListener<String> {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDbChangedMessageListener.class);

    @Inject
    private SubscriptionListUpdater subscriptionListUpdater;

    @Override
    public void onMessage(final String message) {
        if (message.equals(SUBSCRIPTION_UPDATE) ) {
            subscriptionListUpdater.updateSubscription();
        } else {
            log.error("Invalid cluster message received {}", message);
        }
    }

}