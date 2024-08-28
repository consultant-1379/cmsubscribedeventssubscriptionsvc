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
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification
import com.ericsson.oss.mediation.network.api.notifications.NotificationType
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
 * Test class for {@link CppEventChangeProcessor}
 */
class CppEventChangeProcessorSpec extends CdiSpecification {

    @ObjectUnderTest
    CppEventChangeProcessor cppEventChangeProcessor

    @Inject
    private Logger logger

    @Inject
    private SubscriptionService subscriptionService

    def "Successfully processing and filtering CPP event"() {
        given: "A CPP notification exists"
            EventQueue.getInstance().getPushMap().clear()
            NodeNotification nodeNotification = new NodeNotification()
            nodeNotification.setAction(action)
            nodeNotification.setUpdateAttributes(new HashMap<String, String>(){{put("userLabel", "Label")}})
            nodeNotification.setCreationTimestamp(new Date())
            nodeNotification.setFdn("MeContext=Node1,ManagedElement=1")

        and: "3 subscriptions exist"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", "//MeContext/ManagedElement", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", "//MeContext/ManagedElement", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", "//MeContext/ManagedElement", new Scope("BASE_ALL", 0))))
            }}

        when: "The CPP event is sent to be processed"
            cppEventChangeProcessor.processChangeEvent(nodeNotification, subscriptions)

        then: "No exception is thrown"
            noExceptionThrown()

        and: "notification is added to the queue"
            EventQueue.getInstance().getPushMap().get("https://site.com/eventListener/v10").size() == 1

        where:
            action                  | _
            NotificationType.CREATE | _
            NotificationType.UPDATE | _
            NotificationType.DELETE | _
    }

    def "Filtering is not executed when no subscriptions exist in the database"() {
        given: "A CPP notification exists"
            NodeNotification nodeNotification = new NodeNotification()
            nodeNotification.setAction(NotificationType.CREATE)

        and: "No subscriptions exist in the database"
            List<Subscription> subscriptions = new ArrayList<Subscription>()

        when: "The COM ECIM event is sent to be processed"
            cppEventChangeProcessor.processChangeEvent(nodeNotification, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()
    }

    def "Filtering is not executed when event operation type is unsupported"() {
        given: "A CPP notification exists"
            NodeNotification nodeNotification = new NodeNotification()

        and: "The notification type is unsupported"
            nodeNotification.setAction(NotificationType.OVERFLOW)

        and: "3 subscriptions exist"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
            }}

        when: "The CPP event is sent to be processed"
            cppEventChangeProcessor.processChangeEvent(nodeNotification, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()

        and: "Debug log is logged"
            1 * logger.debug("Unsupported CPP operation type: {}", nodeNotification)
    }

}
