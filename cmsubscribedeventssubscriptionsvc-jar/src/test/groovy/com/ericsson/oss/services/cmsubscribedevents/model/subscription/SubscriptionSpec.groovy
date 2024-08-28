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
package com.ericsson.oss.services.cmsubscribedevents.model.subscription

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Test class for {@link Subscription}
 */
class SubscriptionSpec extends CdiSpecification {

    def "Subscription data is modelled correctly"() {

        when: "A Subscription is created"
            Scope scope = new Scope()
            scope.setScopeLevel(0)
            scope.setScopeType("BASE_ALL")

            NtfSubscriptionControl ntfSubscriptionControl = new NtfSubscriptionControl()
            ntfSubscriptionControl.setId(1)
            ntfSubscriptionControl.setNotificationFilter("//MeContext/timestampOfChange")
            ntfSubscriptionControl.setNotificationRecipientAddress("https://idun.ericsson.com/eventListener/v7")
            ntfSubscriptionControl.setNotificationType(Arrays.asList("notifyMOICreation") as String[])
            ntfSubscriptionControl.setObjectClass("/")
            ntfSubscriptionControl.setObjectInstance("/")
            ntfSubscriptionControl.setScope(scope)

            Subscription subscription = new Subscription()
            subscription.setNtfSubscriptionControl(ntfSubscriptionControl)

        then: "the data is modelled correctly"
            scope.getScopeLevel() == 0
            scope.getScopeType() == "BASE_ALL"

            ntfSubscriptionControl.getId() == 1
            ntfSubscriptionControl.getNotificationFilter() == "//MeContext/timestampOfChange"
            ntfSubscriptionControl.getNotificationRecipientAddress() == "https://idun.ericsson.com/eventListener/v7"
            ntfSubscriptionControl.getNotificationTypes() == Arrays.asList("notifyMOICreation") as String[]
            ntfSubscriptionControl.getObjectClass() == "/"
            ntfSubscriptionControl.getObjectInstance() == "/"
            ntfSubscriptionControl.getScope() == scope

            subscription.getNtfSubscriptionControl() == ntfSubscriptionControl
    }

}