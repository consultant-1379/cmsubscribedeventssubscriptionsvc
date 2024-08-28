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


import com.ericsson.cds.cdi.support.providers.custom.sfwk.PropertiesForTest
import com.ericsson.cds.cdi.support.providers.custom.sfwk.SuppliedProperty
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionPersistenceService
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatValidator
import javax.inject.Inject

class SubscriptionServiceBeanSpec extends CdiSpecification {

    private static final String EVENTS_LISTENER_URL = "/eventsListener"
    @Inject
    SubscriptionServiceBean subscriptionServiceBean

    @Inject
    SubscriptionPersistenceService subscriptionPersistenceService

    @MockedImplementation
    HeartbeatValidator heartbeatValidator

    @MockedImplementation
    SubscriptionDbChangedMessageSender subscriptionDbChangedMessageSender


    def "When a Subscription Service create interface is called remote Subscription Persistence Service instance is called"() {
        given: "SubscriptionService is initialized"
            String validSubscription = getValidSubscription()
            subscriptionPersistenceService.createSubscription(validSubscription) >> "xxx"
        and: "Heartbeat is successful"
            heartbeatValidator.isSubscriptionHeartbeatUrlValid(_ as String) >> true
        when: "Create Subscription is invoked"
            subscriptionServiceBean.createSubscription(validSubscription)
        then: "Subscription Persistence Service create is executed"
            1 * subscriptionPersistenceService.createSubscription(validSubscription)
            1 * subscriptionDbChangedMessageSender.sendSignalMessage()
    }

    def "When a Subscription Service create interface is called and heartbeat fails exception is thrown"() {
        given: "SubscriptionService is initialized"
            String validSubscription = getValidSubscription()
            subscriptionPersistenceService.createSubscription(validSubscription) >> "xxx"
        and: "Heartbeat fails"
            heartbeatValidator.isSubscriptionHeartbeatUrlValid(_ as String) >> false
        when: "Create Subscription is invoked"
            subscriptionServiceBean.createSubscription(validSubscription)
        then: "SubscriptionHeartbeatException is thrown"
            thrown(SubscriptionHeartbeatException)
    }

    @PropertiesForTest(properties = [@SuppliedProperty(name = "cmSubscribedEventsHeartbeatInterval", value = "0")])
    def "When a Subscription Service create interface is called and cmSubscribedEventsHeartbeatInterval is set to 0 heartbeat check is not executed"() {
        given: "cmSubscribedEventsHeartbeatInterval is set to 0"
            String validSubscription = getValidSubscription()
        when: "Create Subscription is invoked"
            subscriptionServiceBean.createSubscription(validSubscription)
        then: "Heartbeat validator is not executed"
            0 * heartbeatValidator.isSubscriptionHeartbeatUrlValid(_ as String)
    }

    def "When a Subscription Service view all interface is called remote Subscription Persistence Service instance is called"() {
        when: "View All Subscription is invoked"
            subscriptionServiceBean.viewAllSubscriptions()
        then: "Subscription Persistence Service view is executed"
            1 * subscriptionPersistenceService.viewAllSubscriptions()
    }

    def "When a Subscription Service view interface is called remote Subscription Persistence Service instance is called"() {

        when: "View Subscription is invoked"
            subscriptionServiceBean.viewSubscription(1)
        then: "Subscription Persistence Service view is executed"
            1 * subscriptionPersistenceService.viewSubscription(1)
    }

    def "When a Subscription Service delete interface is called remote Subscription Persistence Service instance is called"() {

        when: "Delete Subscription is invoked"
            subscriptionServiceBean.deleteSubscription(1)
        then: "Subscription Persistence Service delete is executed"
            1 * subscriptionPersistenceService.deleteSubscription(1)
            1 * subscriptionDbChangedMessageSender.sendSignalMessage()
    }

    private static String getValidSubscription() {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", "BASE_ALL", 0)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"notificationTypes\":[\"%s\"],\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com" + EVENTS_LISTENER_URL, 1, "notifyMOICreation", "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }


}