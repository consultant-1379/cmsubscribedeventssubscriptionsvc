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
package com.ericsson.oss.services.cmsubscribedevents.heartbeat

import javax.inject.Inject

import org.junit.Rule

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.ContinuousHeartbeatFailureSubscriptionDeleter
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

class ContinuousHeartbeatFailureSubscriptionDeleterSpec extends CdiSpecification {

    @ObjectUnderTest
    ContinuousHeartbeatFailureSubscriptionDeleter continuousHeartbeatFailureSubscriptionDeleter

    @Inject
    Logger logger

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    private Subscription subscription;

    def setup() {
        System.setProperty("INTERNAL_ALARM_SERVICE_FULL_URL", "http://0.0.0.0:8080/internal-alarm-service")
        Scope scope = new Scope("BASE_ALL", 0)
        NtfSubscriptionControl ntfSubscriptionControl = new NtfSubscriptionControl(1, "http://localhost:8080/eventListener/v1/fm", null, "/", "/", null, scope)
        subscription = new Subscription(ntfSubscriptionControl)
    }

    def "When rest call to generate internal alarm is successfully called and returned response 200, then it is properly logged" () {
        given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 200 OK"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/internal-alarm-service")).willReturn(WireMock.aResponse().withStatus(200)))
        when: "The rest client receives the correct information to generate internal alarm"
            continuousHeartbeatFailureSubscriptionDeleter.deleteSubscription(subscription)

        then: "Mocked status 200 response is received and logged correctly"
            1 * logger.info(String.format('Alarm request with Problem Text \"{}\" processed successfully'), String.format("The endpoint identified by notificationRecipientAddress in Subscription [%s] cannot be reached. Subscription %d has been deleted", 
            new ObjectMapper().writeValueAsString(subscription), subscription.getNtfSubscriptionControl().getId()))
    }

    def "When rest call to generate internal alarm is unsuccessfully called and returned response 400, then it is properly logged" () {
        given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 400 BAD REQUEST"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/internal-alarm-service")).willReturn(WireMock.aResponse().withStatus(400)))
        when: "The rest client receives the correct information to generate internal alarm"
            continuousHeartbeatFailureSubscriptionDeleter.deleteSubscription(subscription)

        then: "Mocked status 400 response is received and logged correctly"
            1 * logger.info(String.format('Alarm request with Problem Text \"{}\" failed to process'),
            String.format("The endpoint identified by notificationRecipientAddress in Subscription [%s] cannot be reached. Subscription %d has been deleted", 
                    new ObjectMapper().writeValueAsString(subscription), subscription.getNtfSubscriptionControl().getId()))
    }
}