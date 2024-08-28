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

package com.ericsson.oss.services.cmsubscribedevents.nbi.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;

/**
 * Used to stored events to be sent to each subscriber.
 */
public class EventQueue {

    private final Map<String,ConcurrentLinkedQueue<String>> pushMap = new ConcurrentHashMap<>();
    private static EventQueue instance;

    public static EventQueue getInstance() {
       if (instance == null) {
               instance = new EventQueue();
       }
       return instance;
    }

    private EventQueue() {}

    public void add(final String recipientAddress, final String entity) {
        ConcurrentLinkedQueue<String> eventData = pushMap.get(recipientAddress);
        if (eventData == null) {
            ConcurrentLinkedQueue<String> newEventData = new ConcurrentLinkedQueue<>();
            newEventData.add(entity);
            pushMap.put(recipientAddress, newEventData);
        } else {
            eventData.add(entity);
        }
    }

    public void removeDeletedHosts(final List<Subscription> subscriptions) {
        final List<String> recipientAddresses = new ArrayList<>();
        for (final Subscription subscription : subscriptions) {
            recipientAddresses.add(subscription.getNtfSubscriptionControl().getNotificationRecipientAddress());
        }
        for (final String host : pushMap.keySet()) {
            if (!recipientAddresses.contains(host)) {
                pushMap.remove(host);
            }
        }
    }

    public Map<String,ConcurrentLinkedQueue<String>> getPushMap() {
        return pushMap;
    }

}
