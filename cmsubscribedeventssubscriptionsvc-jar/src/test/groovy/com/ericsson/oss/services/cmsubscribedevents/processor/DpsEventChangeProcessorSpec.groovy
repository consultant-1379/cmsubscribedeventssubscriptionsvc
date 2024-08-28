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
package com.ericsson.oss.services.cmsubscribedevents.processor

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.notification.event.ActionArgsData
import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsActionPerformedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent
import com.ericsson.oss.itpf.datalayer.dps.stub.StubbedDataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.stub.object.StubbedPoBuilder
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.PushNotificationException
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.filter.SubscriptionFilter
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.nbi.queue.EventQueue

import org.slf4j.Logger

import javax.inject.Inject

/**
 * Test class for {@link DpsEventChangeProcessor}
 */
class DpsEventChangeProcessorSpec extends CdiSpecification {

    @ObjectUnderTest
    DpsEventChangeProcessor dpsEventChangeProcessor

    @Inject
    Logger logger

    def "Successfully processing a DPS event"() {

        given: "A DPS notification exists"
            DpsDataChangedEvent dpsDataChangedEvent = dpsDataChangedEventInstance
            EventQueue.getInstance().getPushMap().clear()

        and: "3 subscriptions exist"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }}

        when: "the DPS event is sent to be processed"
            dpsEventChangeProcessor.processChangeEvent(dpsDataChangedEvent, subscriptions)

        then: "No exception is thrown"
            noExceptionThrown()

        and: "notification is added to the queue"
            EventQueue.getInstance().getPushMap().get("https://site.com/eventListener/v10").size() == 1
        

        where:
            dpsDataChangedEventInstance                                                                                                                                                                         | _
            new DpsObjectCreatedEvent("OSS_NE_DEF", "NetworkElement", "2.0.0", 57000L, "NetworkElement=LTE04dg2ERBS00035", "Live", true, new HashMap<String, String>() {{put("attribute", "value")}})           | _
            new DpsObjectDeletedEvent("OSS_NE_DEF", "NetworkElement", "2.0.0", 57000L, "NetworkElement=LTE04dg2ERBS00035", "Live", true, new HashMap<String, String>() {{put("attribute", "value")}})           | _
            new DpsAttributeChangedEvent("OSS_NE_DEF", "NetworkElement", "2.0.0", 57000L, "NetworkElement=LTE04dg2ERBS00035", "Live", Arrays.asList(new AttributeChangeData("Active", null, true, null, null))) | _
    }

    def "Filtering is not executed when no subscriptions exist in the database"() {
        given: "A DPS notification exists"
            DpsDataChangedEvent dpsDataChangedEvent = new DpsObjectCreatedEvent("OSS_NE_DEF", "NetworkElement", "2.0.0", 57000L, "NetworkElement=LTE04dg2ERBS00035",
                    "Live", true, new HashMap<String, String>() {{put("attribute", "value")}})

        and: "No subscriptions exist in the database"
            List<Subscription> subscriptions = new ArrayList<Subscription>()

        when: "The DPS event is sent to be processed"
            dpsEventChangeProcessor.processChangeEvent(dpsDataChangedEvent, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()
    }

    def "Filtering is not executed when event operation type is unsupported"() {
        given: "An DPS notification with unsupported operation type exists"
            DpsDataChangedEvent unsupportedDpsEvent = new DpsActionPerformedEvent(new StubbedPoBuilder(new StubbedDataPersistenceService()).create(), "NetworkElement=LTE02ERBS00006",
                    "Live", "Action1", Arrays.asList(new ActionArgsData("name", "value")))

        and: "3 subscriptions exist"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
            }}

        when: "The unsupported DPS event is sent to be processed"
            dpsEventChangeProcessor.processChangeEvent(unsupportedDpsEvent, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()

        and: "Debug log is logged"
            1 * logger.debug("Unsupported DPS operation type: {}", unsupportedDpsEvent)
    }   
}
