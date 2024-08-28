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
package com.ericsson.oss.services.cmsubscribedevents.nbi.queue

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.PRESENTATION_SERVER_NAME

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.verify
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ImplementationInstance
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext
import com.ericsson.oss.services.cmsubscribedevents.util.HttpsConfigurator
import com.ericsson.oss.services.cmsubscribedevents.builder.VesEventBuilder
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean
import com.ericsson.oss.services.cmsubscribedevents.stubs.RetryManagerStub
import com.ericsson.oss.services.cmsubscribedevents.stubs.RetryContextStub

import org.junit.Rule

import javax.inject.Inject

import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import javax.net.ssl.SSLContext
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.SSLContexts

class EventQueueSenderEjbSpec extends CdiSpecification {

    public static final String EVENT_LISTENER_V1_URL = "/eventListener/v1"
    public static final String EVENT_LISTENER_V2_URL = "/eventListener/v2"
    public static final String EVENT_LISTENER_V3_URL = "/eventListener/v3"
    public static final String HTTP_LOCALHOST = "http://localhost:"
    public static final String HTTPS_LOCALHOST = "https://localhost:"
    private static final String NOTIFY_MOICHANGES = "notifyMOIChanges"

    private final static String NAMESPACE = "OSS_NE_DEF"
    private final static String NAME = "EUtranCellFDD"
    private final static String VERSION = "2.0.0"
    private final static Long PO_ID = 12345678910L
    private final static String BUCKET_NAME = "LIVE"
    private final static String DPS_FDN = "NetworkElement=NR01gNodeBRadio00001"

    private static final String SERVER_NAME = "serverName"

    @ObjectUnderTest
    EventQueueSenderEjb eventQueueSenderEjb

    @Inject
    VesEventBuilder vesEventBuilder

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean

    @ImplementationInstance
    RetryManager retryManager = new RetryManagerStub()

    @ImplementationInstance
    RetryContext retryContext = new RetryContextStub()

    @MockedImplementation
    HttpsConfigurator httpsConfigurator

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443))


    def setupSpec() {
        System.setProperty(PRESENTATION_SERVER_NAME, SERVER_NAME)
    }

    def "When error sending event, error instrumentation updated"() {

        given: "A valid event event for a subscription"
            httpsConfigurator.isSecure(_ as String) >> false
            String notificationRecipientAddress = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)
            retryManager.setThrowException(true)

        when: "Event sent off to be pushed to recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000)

        then: "Wiremock stub verifies the request and verify wiremock received expected notification type"
            verify(0, postRequestedFor(urlEqualTo(EVENT_LISTENER_V1_URL)))
            eventsInstrumentationBean.getTotalVesEventsPushedError() == 1
    }

    def "When dataQueue has one subscription with one event, verify wiremock with 1 request"() {

        given: "A valid event event for a subscription"
            httpsConfigurator.isSecure(_ as String) >> false
            createMoiChangesListenerStub(EVENT_LISTENER_V1_URL, 204)
            String notificationRecipientAddress = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)

        when: "Event sent off to be pushed to recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000);

        then: "Wiremock stub verifies the request and verify wiremock received expected notification type"
            verify(postRequestedFor(urlEqualTo(EVENT_LISTENER_V1_URL)))
            eventsInstrumentationBean.getTotalVesEventsPushedSuccessfully() == 1
    }

    def "When dataQueue has one subscription with two events, verify wiremock with 2 requests"() {

        given: "A valid event event for a subscription"
            httpsConfigurator.isSecure(_ as String) >> false
            createMoiChangesListenerStub(EVENT_LISTENER_V1_URL, 204)
            String notificationRecipientAddress = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)

        when: "Event sent off to be pushed to recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000);

        then: "Wiremock stub verifies the request and verify wiremock received expected notification type"
            verify(2, postRequestedFor(urlEqualTo(EVENT_LISTENER_V1_URL)))
            eventsInstrumentationBean.getTotalVesEventsPushedSuccessfully() == 2
    }

    def "When dataQueue has three subscription with one event, verify wiremock with 3 requests"() {

        given: "A valid event event for a subscription"
            httpsConfigurator.isSecure(_ as String) >> false
            createMoiChangesListenerStub(EVENT_LISTENER_V1_URL, 204)
            createMoiChangesListenerStub(EVENT_LISTENER_V2_URL, 204)
            createMoiChangesListenerStub(EVENT_LISTENER_V3_URL, 204)
            String notificationRecipientAddress1 = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL
            String notificationRecipientAddress2 = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V2_URL
            String notificationRecipientAddress3 = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V3_URL
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress1, eventWrapper)
            EventQueue.getInstance().add(notificationRecipientAddress2, eventWrapper)
            EventQueue.getInstance().add(notificationRecipientAddress3, eventWrapper)

        when: "Event sent off to be pushed to recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000);

        then: "Wiremock stub verifies the request and verify wiremock received expected notification type"
            verify(1, postRequestedFor(urlEqualTo(EVENT_LISTENER_V1_URL)))
            verify(1, postRequestedFor(urlEqualTo(EVENT_LISTENER_V2_URL)))
            verify(1, postRequestedFor(urlEqualTo(EVENT_LISTENER_V3_URL)))
            eventsInstrumentationBean.getTotalVesEventsPushedSuccessfully() == 3
    }

    def "When dataQueue has one subscription with one event and response is not 204, push errors incremented"() {

        given: "A valid event event for a subscription"
            httpsConfigurator.isSecure(_ as String) >> false
            createMoiChangesListenerStub(EVENT_LISTENER_V1_URL, 201)
            String notificationRecipientAddress = HTTP_LOCALHOST + wireMockRule.port() + EVENT_LISTENER_V1_URL
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)

        when: "Event sent off to be pushed to recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000)

        then: "Wiremock stub verifies the request and verify wiremock received expected notification type"
            verify(postRequestedFor(urlEqualTo(EVENT_LISTENER_V1_URL)))
            eventsInstrumentationBean.getTotalVesEventsPushedError() == 1
    }

    def "When secure host is provided, ssl configuration is used"() {

        given: "A valid event event for a subscription that points to a https listener"
            SSLContextBuilder sslContextBuilder = SSLContexts.custom()
            SSLContext sslContext1 = sslContextBuilder.build()

            httpsConfigurator.isSecure(_ as String) >>true
            httpsConfigurator.getSSLConnectionSocketFactory() >> new SSLConnectionSocketFactory(sslContext1, new NoopHostnameVerifier())

            createMoiChangesListenerStub(EVENT_LISTENER_V1_URL, 204)
            String notificationRecipientAddress = HTTPS_LOCALHOST + wireMockRule.httpsPort() + EVENT_LISTENER_V1_URL

            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(["string": "string"])
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)
            EventQueue.getInstance().getPushMap().clear()
            EventQueue.getInstance().add(notificationRecipientAddress, eventWrapper)

        when: "Event sent off to be pushed to the https recipient address"
            eventQueueSenderEjb.coordinatePushEventsThreads()
            Thread.sleep(3000)

        then: "SSL Context for connection is requested"
            1 * httpsConfigurator.getSSLConnectionSocketFactory()
    }

    private static StubMapping createMoiChangesListenerStub(String eventListenerURL, final int responseCode) {
        stubFor(commonWiremockStubRequestBody(eventListenerURL)
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.notificationType", equalTo("notifyMOIChanges")))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.eventTime", matching(/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/)))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.moiChanges"))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.moiChanges.[0].notificationId", matching(/\d*/)))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.moiChanges.[0].path", matching(/\S*/)))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.moiChanges.[0].operation", matching(/^(CREATE|DELETE|REPLACE)$/)))
            .willReturn(aResponse().withStatus(responseCode)))
    }

    private static MappingBuilder commonWiremockStubRequestBody(String eventListenerURL) {
        return  post(urlPathEqualTo(eventListenerURL))
            .withRequestBody(matchingJsonPath("\$.event"))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.version", equalTo("4.1")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.vesEventListenerVersion", equalTo("7.2.1")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.domain", equalTo("stndDefined")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.stndDefinedNamespace", equalTo("3GPP-Provisioning")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.eventName", equalTo("Provisioning_ENM-Ericsson_VES")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.eventId", matching(/^Provisioning_\S*/)))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.sequence", matching(/\d*/)))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.priority", equalTo("High")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.reportingEntityName", equalTo("NR01gNodeBRadio00001")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.nfVendorName", equalTo("Ericsson")))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.startEpochMicrosec", matching(/\d*/)))
            .withRequestBody(matchingJsonPath("\$.event.commonEventHeader.lastEpochMicrosec", matching(/\d*/)))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.stndDefinedFieldsVersion", equalTo("1.0")))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data"))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.href", matching(/\S*/)))
            .withRequestBody(matchingJsonPath("\$.event.stndDefinedFields.data.notificationId", matching(/\d*/)))
    }

    private static DpsDataChangedEvent setupDpsNotification(final Map<String, Object> attributesForOperation) {
        return new DpsObjectCreatedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, false, attributesForOperation)
    }
}