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

package com.ericsson.oss.services.cmsubscribedevents.nbi

import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_GENERAL_ERROR
import static com.ericsson.oss.services.cmsubscribedevents.api.constants.ExceptionMessageConstants.HEARTBEAT_TIMEOUT_ERROR
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.serverError
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.SubscriptionHeartbeatException
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean
import com.ericsson.oss.services.cmsubscribedevents.util.HttpsConfigurator
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import org.apache.http.entity.StringEntity
import org.junit.Rule

import spock.lang.Unroll

class HeartbeatNotificationSenderSpec extends CdiSpecification {

    private static final String VALID_NOTIFICATION_HEARTBEAT_JSON = '{"event":{"commonEventHeader":{"version":"4.1","vesEventListenerVersion":"7.2.1","domain":"heartbeat","stndDefinedNamespace":"3GPP-Heartbeat","eventName":"Heartbeat_ENM-Ericsson_VES","eventId":"Heartbeat_048d070f-4e14-4005-bfeb-45bde8b76f82","sequence":0,"priority":"High","reportingEntityName":"serverName","sourceName":"serverName","nfVendorName":"Ericsson","startEpochMicrosec":1677494174294000,"lastEpochMicrosec":1677494174294000},"stndDefinedFields":{"stndDefinedFieldsVersion":"1.0","data":{"href":"serverName","notificationId":3884588823874260242,"notificationType":"Heartbeat","eventTime":"27-FEB-2023 10:36:14","systemDN":"serverName","heartbeatNtfPeriod":10}}}}'
    private static final String HTTP_LOCALHOST = "http://localhost:"
    private static final String HTTPS_LOCALHOST = "https://localhost:"
    private static final int DEFAULT_TIMEOUT = 10
    private static final String EVENT_LISTENER_V1_URL = "/eventListener/v1"

    @ObjectUnderTest
    HeartbeatNotificationSender  heartbeatNotificationSender

    @MockedImplementation
    HttpsConfigurator httpsConfigurator

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "A valid heartbeat notification is sent to a valid eventListener URL"() {
        given: "An eventListener URL"
            httpsConfigurator.isSecure(_ as String) >> false
            createEventListenerStub(EVENT_LISTENER_V1_URL)
        and: "A valid heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity(VALID_NOTIFICATION_HEARTBEAT_JSON)
        when: "A heartbeat notification is sent"
            boolean returnedResult = heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, DEFAULT_TIMEOUT)
        then: "Heartbeat notification returns true"
            returnedResult
    }

    def "A valid heartbeat notification is sent to an eventListener URL that throws a server error"() {
        given: "An eventListener URL that returns a server error"
            httpsConfigurator.isSecure(_ as String) >> false
            stubFor(post(urlPathEqualTo(EVENT_LISTENER_V1_URL)).willReturn(serverError()))
        and: "A valid heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity(VALID_NOTIFICATION_HEARTBEAT_JSON)
        when: "A heartbeat notification is sent"
            boolean returnedResult = heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, DEFAULT_TIMEOUT)
        then: "Heartbeat notification returns false"
            !returnedResult
    }

    def "A valid heartbeat notification is sent to an eventListener URL that throws a timeout error "() {
        given: "An eventListener URL that returns a timeout server error"
            httpsConfigurator.isSecure(_ as String) >> false
            stubFor(post(urlPathEqualTo(EVENT_LISTENER_V1_URL)).willReturn(aResponse()
                    .withStatus(404)
                    .withFixedDelay(3000)))
        and: "A valid heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity(VALID_NOTIFICATION_HEARTBEAT_JSON)
        when: "A heartbeat notification is sent"
            heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, 2) // Not set to default to shorten test case execution time
        then: "Heartbeat notification throws a HeartbeatException"
            def subscriptionHeartbeatException = thrown(SubscriptionHeartbeatException)
        and: "Heartbeat exception contains the expected error message content"
            subscriptionHeartbeatException.getMessage() == HEARTBEAT_TIMEOUT_ERROR
    }

    @Unroll
    def "A valid heartbeat notification is sent to an eventListener URL that #faultDescription"() {
        given: "An eventListener URL that returns an unexpected response"
            httpsConfigurator.isSecure(_ as String) >> false
            stubFor(post(urlPathEqualTo(EVENT_LISTENER_V1_URL)).willReturn(aResponse().withFault(faultType)))
        and: "A heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity(VALID_NOTIFICATION_HEARTBEAT_JSON)
        when: "A heartbeat notification is sent"
            heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, DEFAULT_TIMEOUT)
        then: "Heartbeat notification throws a HeartbeatException"
            def subscriptionHeartbeatException = thrown(SubscriptionHeartbeatException)
        and: "Heartbeat exception contains the expected error message content"
            subscriptionHeartbeatException.getMessage() == HEARTBEAT_GENERAL_ERROR
        where:
            faultType                      | faultDescription      | _
            Fault.CONNECTION_RESET_BY_PEER | "resets connection"   | _
            Fault.RANDOM_DATA_THEN_CLOSE   | "returns random data" | _
            Fault.EMPTY_RESPONSE           | "an empty response"   | _

    }

    def "An invalid heartbeat notification is sent to a valid eventListener URL"() {
        given: "A valid eventListener URL"
            httpsConfigurator.isSecure(_ as String) >> false
            createEventListenerStub(EVENT_LISTENER_V1_URL)
            stubFor(post(urlPathEqualTo(EVENT_LISTENER_V1_URL)).withRequestBody(equalToJson("{}")).willReturn(aResponse().withStatus(404)))
        and: "An invalid heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity("{}")
        when: "A heartbeat notification is sent"
            boolean returnedResult = heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, DEFAULT_TIMEOUT)
        then: "Heartbeat result is false"
            !returnedResult
    }

    def "When secure host is provided, ssl configuration is used"() {
        given: "The check for secure address will pass"
            httpsConfigurator.isSecure(_ as String) >> true
        and: "A valid heartbeat notification body"
            StringEntity notificationBodyEntity = new StringEntity(VALID_NOTIFICATION_HEARTBEAT_JSON)
        when: "A heartbeat notification is sent"
                heartbeatNotificationSender.isHeartbeatNotificationSuccessful(HTTPS_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL, notificationBodyEntity, DEFAULT_TIMEOUT)
        then: "ssl details are requested"
            SubscriptionHeartbeatException she = thrown()
            she.getMessage() == "Unable to establish heartbeat connection."
            1 * httpsConfigurator.getSSLConnectionSocketFactory()
    }

    private static StubMapping createEventListenerStub(String eventListenerURL) {
        stubFor(post(urlPathEqualTo(eventListenerURL))
                .withRequestBody(matchingJsonPath("\$.event"))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.version", equalTo("4.1")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.vesEventListenerVersion", equalTo("7.2.1")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.domain", equalTo("heartbeat")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.stndDefinedNamespace", equalTo("3GPP-Heartbeat")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.eventName", equalTo("Heartbeat_ENM-Ericsson_VES")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.eventId", matching(/^Heartbeat_\S*/)))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.sequence", equalTo('0')))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.priority", equalTo("High")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.reportingEntityName", equalTo("serverName")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.sourceName", matching(/\S*/)))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.nfVendorName", equalTo("Ericsson")))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.startEpochMicrosec", matching(/\d*/)))
                .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.lastEpochMicrosec", matching(/\d*/)))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.stndDefinedFieldsVersion", equalTo("1.0")))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data"))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.href", equalTo("serverName")))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.notificationId", matching(/\d*/)))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.notificationType", equalTo("Heartbeat")))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.eventTime", matching(/[0-9]{2}-[A-Z]{3}-[0-9]{4} (2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]/)))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.systemDN", equalTo("serverName")))
                .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.heartbeatNtfPeriod", equalTo('10')))
                .willReturn(aResponse().withStatus(204)))
    }

}