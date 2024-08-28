/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.util

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

class SubscriptionJsonParserSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscriptionJsonParser subscriptionJsonParser

    def "Retrieve a notificationRecipientAddress from a subscription with a valid notificationRecipientAddress"() {
        given: "A valid subscription is inputted"
        when: "SubscriptionJsonParser retrieves the notificationRecipientAddress"
            String returnedNotificationRecipientAddress = subscriptionJsonParser.getNotificationReceiptAddressValue(subscriptionJsonString)
        then: "NotificationRecipientAddress is returned"
            returnedNotificationRecipientAddress == expectedNotificationRecipientAddress
        where:
            expectedNotificationRecipientAddress | subscriptionJsonString                                                                                                                                                                                                                       | _
            "https://site.com/eventListener/v10" | '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"1","notificationTypes":["notifyMOICreation"],"objectClass":"/","objectInstance":"/","scope":{"scopeType":"BASE_ALL","scopeLevel":0}}}' | _
            "https://site.com/eventListener/v11" | '{"ntfSubscriptionControl":{"id":"1","notificationRecipientAddress":"https://site.com/eventListener/v11","notificationTypes":["notifyMOICreation"],"objectClass":"/","objectInstance":"/","scope":{"scopeType":"BASE_ALL","scopeLevel":0}}}' | _
            "https://site.com/eventListener/v12" | '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v12","notificationTypes":["notifyMOICreation"]}}'                                                                                                 | _
            "x"                                  | '{"ntfSubscriptionControl":{"notificationRecipientAddress":"x","notificationTypes":["notifyMOICreation"]}}'                                                                                                                                  | _
    }

    def "Retrieve a notificationRecipientAddress from a subscription with an invalid notificationRecipientAddress"() {
        given: "An invalid subscription is inputted"
        when: "SubscriptionJsonParser retrieves the notificationRecipientAddress"
            subscriptionJsonParser.getNotificationReceiptAddressValue(subscriptionJsonString)
        then: "An exception is thrown"
            thrown(IllegalArgumentException)
        where:
            subscriptionJsonString                                                                                                                                                                                                                      | _
            '{"ntfSubscriptionControl":"notificationRecipientAddress":"https://site.com/eventListener/v20","id":"1","notificationTypes":["notifyMOICreation"],"objectClass":"/","objectInstance":"/","scope":{"scopeType":"BASE_ALL","scopeLevel":0}}}' | _
            '{"ntfSubscriptionControl":{"notificationRecipientAddress":"","notificationTypes":["notifyMOICreation"]}}'                                                                                                                                  | _
            '{"ntfSubscriptionControl":{"notificationRecipientInvalidAddress":"","notificationTypes":["notifyMOICreation"]}}'                                                                                                                           | _

    }

}
