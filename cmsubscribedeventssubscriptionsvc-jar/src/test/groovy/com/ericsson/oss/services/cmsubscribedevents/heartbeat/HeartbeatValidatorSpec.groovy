/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */
package com.ericsson.oss.services.cmsubscribedevents.heartbeat

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import javax.inject.Inject
import org.junit.Rule

class HeartbeatValidatorSpec extends CdiSpecification {

    @ObjectUnderTest
    HeartbeatValidator heartbeatValidator

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "A subscription with an invalid notificationRecipientAddress URL executes heartbeatValidation"() {
        given: "A subscription is created with an invalid notificationRecipientAddress URL"
        when: "Heartbeat validation is executed"
            heartbeatValidator.isSubscriptionHeartbeatUrlValid(subscriptionNotificationRecipientAddress)
        then: "An exception is thrown"
            def subscriptionHeartbeatException = thrown(SubscriptionHeartbeatException)
        and: "Exception returns the expected message"
            subscriptionHeartbeatException.getMessage() == "Failed to create subscription. Invalid URL for notificationRecipientAddress."
        where:
            subscriptionNotificationRecipientAddress | _
            "xyz"                                    | _
            ""                                       | _
    }

    def "A subscription with a valid notificationRecipientAddress URL executes heartbeatValidation"() {
        given: "A valid notificationRecipientAddress URL"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(204)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
        when: "Heartbeat validation is executed"
            boolean isHeartbeatSuccessful = heartbeatValidator.isSubscriptionHeartbeatUrlValid(subscriptionNotificationRecipientAddress)
        then: "An exception is not thrown"
            notThrown(SubscriptionHeartbeatException)
        and: "Heartbeat validation is successful"
            isHeartbeatSuccessful
            eventsInstrumentationBean.getTotalSuccessfulHeartbeatRequests() == 1
    }

    def "HeartbeatValidation fails if received any response code other than 204"() {
        given: "A valid notificationRecipientAddress URL"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(404)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
        when: "Heartbeat validation is executed"
            boolean isHeartbeatSuccessful = heartbeatValidator.isSubscriptionHeartbeatUrlValid(subscriptionNotificationRecipientAddress)
        then: "An exception is not thrown"
            notThrown(SubscriptionHeartbeatException)
        and: "Heartbeat validation is successful"
            isHeartbeatSuccessful == false
            eventsInstrumentationBean.getTotalFailedHeartbeatRequests() == 1
    }
}
