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

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.cds.cdi.support.rule.MockedImplementation

import com.ericsson.oss.services.cmsubscribedevents.ejb.SubscriptionDbChangedMessageListener

import java.util.Collections;
import javax.inject.Inject

class SubscriptionDbChangedMessageListenerSpec extends CdiSpecification {

    @Inject
    SubscriptionDbChangedMessageListener subscriptionDbChangedMessageListener

    @MockedImplementation
    SubscriptionListUpdater subscriptionListUpdater

    def "When message received, subscription list is updated if valid message"() {
        when: "update subscriptions is called"
            subscriptionDbChangedMessageListener.onMessage(dbChangeType)
        then: "processors are updated also"
            invocation * subscriptionListUpdater.updateSubscription()
        where:
            dbChangeType          | invocation
            "CREATE"              | 0
            "DELETE"              | 0
            "SUBSCRIPTION_UPDATE" | 1
    }
}