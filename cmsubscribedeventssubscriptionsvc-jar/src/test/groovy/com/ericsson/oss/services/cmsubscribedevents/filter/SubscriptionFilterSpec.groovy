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
package com.ericsson.oss.services.cmsubscribedevents.filter

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription

/**
 * Test class for {@link SubscriptionFilter}
 */
class SubscriptionFilterSpec extends CdiSpecification {

    def "Subscriptions filtered successfully by notificationType"() {

        when: "the subscriptions are sent to be filtered"
            List<Subscription> filteredSubscriptions = SubscriptionFilter.filter(subscriptionsToBeFiltered as List<Subscription>, eventOperationType, "MeContext=LTE02ERBS00006,ManagedElement=1")

        then: "the subscription not matching the notificationType criteria is removed"
            filteredSubscriptions.get(0).getNtfSubscriptionControl().getId() != 4
            filteredSubscriptions.get(1).getNtfSubscriptionControl().getId() != 4

        and: "only two (out of three) subscriptions should be returned after filtering"
            filteredSubscriptions.size() == 2

        where:
            subscriptionsToBeFiltered | eventOperationType
            new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }} | OperationType.CREATE

            new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }} | OperationType.REPLACE

            new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }} | OperationType.DELETE

            new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIChanges", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOIChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }} | OperationType.DELETE

            new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIChanges", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", null, "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }} | OperationType.REPLACE
    }

    def "Subscriptions filtered successfully by notificationFilter"() {
        given: "A list of Subscriptions exist"
            final List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(2, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement ", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v1", null, "/", "/", " //SubNetwork/ManagedElement ", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement | //MeContext/MangedElement/ENodeBFunction | //MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(5, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement|//MeContext/MangedElement/ENodeBFunction|//MeContext|//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(6, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement/", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(7, "https://site.com/eventListener/v1", null, "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(8, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElemen", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(9, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/managedElement", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(10, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement/ManagedElement", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(11, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement/ENodeBFunction/", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(12, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement/ENodeBFunction", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(13, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElement/ENodeBFunction/EUtranCellFDD", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(14, "https://site.com/eventListener/v1", null, "/", "/", "//SubNetwork/ManagedElemen | //MeContext/", new Scope("BASE_ALL", 0))))
            }}

        when: "The subscriptions are sent to be filtered"
            List<Subscription> filteredSubscriptions = SubscriptionFilter.filter(subscriptions, OperationType.CREATE, "SubNetwork=AP_6813455,ManagedElement=LTE04dg2.ERBS00035-1")

        then: "the subscriptions not matching the notificationFilter criteria are removed"
            def expectedToBeRemovedSubscriptionIds = [8, 9, 10, 11, 12, 13, 14]
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(0).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(1).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(2).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(3).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(4).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(5).getNtfSubscriptionControl().getId())
            !expectedToBeRemovedSubscriptionIds.contains(filteredSubscriptions.get(6).getNtfSubscriptionControl().getId())

        and: "only seven (out of fourteen) subscriptions should be returned after filtering"
            filteredSubscriptions.size() == 7
    }
}