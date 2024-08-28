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
package com.ericsson.oss.services.cmsubscribedevents.ejb

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService

class SubscriptionListUpdaterSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscriptionListUpdater subscriptionListUpdater

    @MockedImplementation
    SubscriptionService subscriptionService

    def setup () {
        subscriptionService.viewAllSubscriptions() >> "[{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"http://141.137.232.12:9004/eventListener/v1/SUB1\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"9\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}]"
    }

    def "When update subscription called, all event processors get updated"() {
        when: "update subscriptions is calledon startup"
            subscriptionListUpdater.updateSubscription()
        then: "no error is thrown"
            noExceptionThrown()
    }
}
