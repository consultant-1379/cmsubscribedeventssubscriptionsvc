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

package com.ericsson.oss.services.cmsubscribedevents.filter;

import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType;
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Responsible for filtering subscription related actions.
 */
public final class SubscriptionFilter {

    private SubscriptionFilter() {}

    /**
     * Responsible for filtering subscriptions based on an incoming event.
     *
     * @param subscriptions The list of subscriptions to filter.
     * @param eventOperationType The event's operation type used for filtering the subscriptions.
     * @param eventHref the event's href used for filtering by the subscription's notificationFilter property.
     * @return The list of filtered subscriptions.
     */
    public static List<Subscription> filter(final List<Subscription> subscriptions, final OperationType eventOperationType, final String eventHref) {
        return subscriptions.parallelStream()
                .filter(subscription -> (filterByNotificationType(subscription.getNtfSubscriptionControl().getNotificationTypes(), eventOperationType)
                        && (filterByNotificationFilter(subscription.getNtfSubscriptionControl().getNotificationFilter(), eventHref))) )
                .collect(Collectors.toList());
    }

    private static boolean filterByNotificationType(final String[] subscriptionNotificationTypes, final OperationType eventOperationType) {
        if(subscriptionNotificationTypes == null) {
            return true;
        }
        for (final String subscriptionNotificationType: subscriptionNotificationTypes) {
            if(subscriptionNotificationType.equals(OperationType.ALL_CHANGES.getNotificationType()) || subscriptionNotificationType.equals(eventOperationType.getNotificationType())) {
                return true;
            }
        }
        return false;
    }

    private static boolean filterByNotificationFilter(final String subscriptionNotificationFilter, final String eventHref) {
        return subscriptionNotificationFilter == null || xPathsMatch(subscriptionNotificationFilter, convertHrefToXpath(eventHref));
    }

    private static String convertHrefToXpath(final String href) {
        return StringUtils.removeEnd("//" + href.replaceAll("=([A-Za-z0-9._-]+,?)", "\\/"), "/");
    }

    private static boolean xPathsMatch(final String notificationFilter, final String eventHrefAsXpath) {
        return Pattern.compile(eventHrefAsXpath + "(?!(/[A-Za-z0-9]))")
                .matcher(notificationFilter)
                .find();
    }

}