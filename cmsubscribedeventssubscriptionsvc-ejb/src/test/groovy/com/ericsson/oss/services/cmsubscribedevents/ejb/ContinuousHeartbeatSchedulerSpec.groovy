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
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.ContinuousHeartbeatFailureMap
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.ContinuousHeartbeatFailureSubscriptionDeleter
import com.ericsson.oss.services.cmsubscribedevents.heartbeat.HeartbeatValidator
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription

import javax.annotation.Resource
import javax.inject.Inject

import org.junit.Rule
import org.slf4j.Logger

import javax.ejb.TimerService
import javax.ejb.Timer

import spock.lang.Shared

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule

class ContinuousHeartbeatSchedulerSpec extends CdiSpecification {

    @Shared
    Logger logger = Mock(Logger)

    @Resource
    TimerService timerService

    @ObjectUnderTest
    ContinuousHeartbeatScheduler scheduler

    @Inject
    Timer timer

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean

    @Inject
    HeartbeatValidator heartbeatValidator

    @Inject
    SubscriptionService subscriptionService

    @MockedImplementation
    SubscriptionListUpdater subscriptionListUpdater

    @Inject
    ContinuousHeartbeatFailureMap continuousHeartbeatFailureMap;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def setup() {
        scheduler.logger = logger
        timerService = Mock(TimerService.class)
        timer = Mock(Timer.class)
        scheduler.timerService = timerService
        scheduler.eventsInstrumentationBean = eventsInstrumentationBean
        scheduler.heartbeatValidator = heartbeatValidator
        scheduler.subscriptionListUpdater = subscriptionListUpdater
        scheduler.continuousHeartbeatFailureMap = continuousHeartbeatFailureMap
        System.setProperty("INTERNAL_ALARM_SERVICE_FULL_URL", "http://0.0.0.0:8080/internal-alarm-service")
    }

    def "When startContinuousHeartbeatTimer is invoked, then a timer is created only if 'cmSubscribedEventsHeartbeatInterval' pib parameter has a value not equal to '0'"() {

        given: "'cmSubscribedEventsHeartbeatInterval' is set to 0"
            scheduler.heartbeatIntervalChangeListener.cmSubscribedEventsHeartbeatInterval = heartbeatIntervalPibValue

        when: "startContinuousHeartbeatTimer is invoked"
            scheduler.startContinuousHeartbeatTimer()

        then: "A timer is not created'"
            timerCreated * timerService.createTimer(_ as Long, _ as Long, "ContinuousHeartbeatTimer")

        where:
            heartbeatIntervalPibValue | timerCreated
            0                         | 0
            10                        | 1
            30                        | 1
            -1                        | 0
    }

    def "When continuousHeartbeatSchedulerTimeout method is invoked and continuous heartbeat is successful, then successfulContinuousHeartbeat metric is incremented"() {
        given: "Valid subscription exists and continuous heartbeat is successful for it's notificationRecipientAddress"
            scheduler.membershipListener.isMaster() >> true
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(204)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
            scheduler.subscriptionListUpdater.getUpdatedSubscriptionList() >> new ArrayList<Subscription>(){{ add(new Subscription(new NtfSubscriptionControl(1, subscriptionNotificationRecipientAddress, Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))}}

        when: "continuousHeartbeatSchedulerTimeout is invoked"
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "successfulContinuousHeartbeat metric is incremented"
            eventsInstrumentationBean.getSuccessfulContinuousHeartbeatRequests() == 1
    }

    def "When continuousHeartbeatSchedulerTimeout method is invoked and continuous heartbeat failed, then failedContinuousHeartbeat metric is incremented"() {
        given: "Valid subscription exists and continuous heartbeat fails for it's notificationRecipientAddress"
            continuousHeartbeatFailureMap.clearSubscriptionById(1)
            scheduler.membershipListener.isMaster() >> true
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(404)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
            scheduler.subscriptionListUpdater.getUpdatedSubscriptionList() >> new ArrayList<Subscription>(){{ add(new Subscription(new NtfSubscriptionControl(1, subscriptionNotificationRecipientAddress, Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))}}

        when: "continuousHeartbeatSchedulerTimeout is invoked"
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "failedContinuousHeartbeat metric is incremented"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 1
            continuousHeartbeatFailureMap.shouldSubscriptionBeDeleted(1) == false

    }

    def "When continuousHeartbeatSchedulerTimeout method is invoked and connection times out, then failedContinuousHeartbeat metric is incremented"() {
        given: "Valid subscription exists and continuous heartbeat fails for it's notificationRecipientAddress"
            continuousHeartbeatFailureMap.clearSubscriptionById(1)
            scheduler.membershipListener.isMaster() >> true
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(204).withFixedDelay(11000)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
            scheduler.subscriptionListUpdater.getUpdatedSubscriptionList() >> new ArrayList<Subscription>(){{ add(new Subscription(new NtfSubscriptionControl(1, subscriptionNotificationRecipientAddress, Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))}}

        when: "continuousHeartbeatSchedulerTimeout is invoked"
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "failedContinuousHeartbeat metric is incremented"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 1
            continuousHeartbeatFailureMap.shouldSubscriptionBeDeleted(1) == false
    }

    def "When continuousHeartbeatSchedulerTimeout method is invoked and continuous heartbeat failed 3 times, then failedContinuousHeartbeat metric is incremented and subscription deleted"() {
        given: "Valid subscription exists and continuous heartbeat fails for it's notificationRecipientAddress"
            continuousHeartbeatFailureMap.clearSubscriptionById(1)
            scheduler.membershipListener.isMaster() >> true
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(404)))
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/internal-alarm-service")).willReturn(WireMock.aResponse().withStatus(200)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
            scheduler.subscriptionListUpdater.getUpdatedSubscriptionList() >> new ArrayList<Subscription>(){{ add(new Subscription(new NtfSubscriptionControl(1, subscriptionNotificationRecipientAddress, Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))}}

        when: "continuousHeartbeatSchedulerTimeout is invoked 3 times"
            scheduler.continuousHeartbeatSchedulerTimeout(timer)
            scheduler.continuousHeartbeatSchedulerTimeout(timer)
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "failedContinuousHeartbeat metric is incremented"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 3
            1 * subscriptionService.deleteSubscription(1)
    }

    def "When continuousHeartbeatSchedulerTimeout method is invoked and continuous heartbeat failed 3 times but not in a row, then failedContinuousHeartbeat metric is incremented and subscription not deleted"() {
        given: "Valid subscription exists and continuous heartbeat fails for it's notificationRecipientAddress"
            continuousHeartbeatFailureMap.clearSubscriptionById(1)
            scheduler.membershipListener.isMaster() >> true
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(404)))
            String subscriptionNotificationRecipientAddress = "http://localhost:" + wireMockRule.port() + "/eventListener/v1"
            scheduler.subscriptionListUpdater.getUpdatedSubscriptionList() >> new ArrayList<Subscription>(){{ add(new Subscription(new NtfSubscriptionControl(1, subscriptionNotificationRecipientAddress, Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))}}

        when: "continuousHeartbeatSchedulerTimeout is invoked 2 times"
            scheduler.continuousHeartbeatSchedulerTimeout(timer)
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "failedContinuousHeartbeat metric is incremented"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 2
            eventsInstrumentationBean.getSuccessfulContinuousHeartbeatRequests() == 0
            0 * subscriptionService.deleteSubscription(1)

        and: "heartbeat is successful on next check"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(204)))
            scheduler.continuousHeartbeatSchedulerTimeout(timer)

        then: "subscription is not deleted"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 2
            eventsInstrumentationBean.getSuccessfulContinuousHeartbeatRequests() == 1
            0 * subscriptionService.deleteSubscription(1)
            
        and: "heartbeat is successful on next check"
            WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/eventListener/v1")).withRequestBody(WireMock.matchingJsonPath("\$.event")).willReturn(WireMock.aResponse().withStatus(404)))
            scheduler.continuousHeartbeatSchedulerTimeout(timer)
   
        then: "subscription is not deleted"
            eventsInstrumentationBean.getFailedContinuousHeartbeatRequests() == 3
            eventsInstrumentationBean.getSuccessfulContinuousHeartbeatRequests() == 1
            0 * subscriptionService.deleteSubscription(1)
    }

    def "When changeInterval is called, existing timer is cancelled and new timer is created with updated interval"() {
        given: "Timer is existing"
            Timer timer = Mock(Timer.class)
            scheduler.timerService.getTimers() >> [timer]
            timer.getInfo() >> "ContinuousHeartbeatTimer"
            scheduler.heartbeatIntervalChangeListener.cmSubscribedEventsHeartbeatInterval = updatedHeartbeatIntervalValue

        when: "ChangeInterval is triggered"
            scheduler.changeInterval()

        then: "Existing timer is cancelled. A new timer is created with updated interval only if the updated interval value is not equal to '0'"
            existingTimerCanccelled * timer.cancel()
            newTimerCreated * timerService.createTimer(updatedHeartbeatIntervalValue * 1000L, updatedHeartbeatIntervalValue * 1000L, 'ContinuousHeartbeatTimer')

        where:
            updatedHeartbeatIntervalValue | existingTimerCanccelled | newTimerCreated
            45                            | 1                       | 1
            0                             | 1                       | 0
            30                            | 1                       | 1
            -1                            | 1                       | 0

    }
}
