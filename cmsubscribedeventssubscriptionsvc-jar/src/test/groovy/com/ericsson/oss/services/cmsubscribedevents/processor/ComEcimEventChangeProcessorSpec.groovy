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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification
import com.ericsson.oss.mediation.notifications.ComEcimNotificationType
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.PushNotificationException
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.filter.SubscriptionFilter
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.nbi.ComEcimEventNotificationProducer
import com.ericsson.oss.services.cmsubscribedevents.nbi.queue.EventQueue

import org.slf4j.Logger

import javax.inject.Inject

/**
 * Test class for {@link ComEcimEventChangeProcessor}
 */
class ComEcimEventChangeProcessorSpec extends CdiSpecification {

    @ObjectUnderTest
    ComEcimEventChangeProcessor comEcimEventChangeProcessor

    @Inject
    private Logger logger

    @Inject
    private ComEcimEventNotificationProducer comEcimEventNotificationProducer
    
    def setup() {
        comEcimEventChangeProcessor.comEcimEventNotificationSender = comEcimEventNotificationProducer
    }

    def "Successfully processing and filtering COM ECIM event"() {
        given: "A COM ECIM notification exists"
            EventQueue.getInstance().getPushMap().clear()
            ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification("NetworkElement=LTE04dg2ERBS00035", 2L, "2022-11-06T12:57:29Z", 170303357101647L, false)
            comEcimNodeNotification.setUpdateAttributes(new HashMap<String, String>(){{put("userLabel", "Label")}})
            comEcimNodeNotification.setDn("SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00001,ManagedElement=NR01gNodeBRadio00001,GNBCUCPFunction=1,NRCellCU=3,NRCellRelation=1")
            comEcimNodeNotification.setAction(action)

        and: "3 subscriptions exist"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", null, new Scope("BASE_ALL", 0))))
            }}

        when: "The COM ECIM event is sent to be processed"
            comEcimEventChangeProcessor.processChangeEvent(comEcimNodeNotification, subscriptions)

        then: "No exception is thrown"
            noExceptionThrown()

        and: "notification is added to attempted to be added to the queue"
            EventQueue.getInstance().getPushMap().get("https://site.com/eventListener/v10").size() == 1
        where:
            action                         | _
            ComEcimNotificationType.CREATE | _
            ComEcimNotificationType.UPDATE | _
            ComEcimNotificationType.DELETE | _

    }

    def "Filtering is not executed when no subscriptions exist in the database"() {
        given: "A COM ECIM notification exists"
            ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification("NetworkElement=LTE04dg2ERBS00035", 2L, "2022-11-06T12:57:29Z", 170303357101647L, false)

        and: "No subscriptions exist in the database"
            List<Subscription> subscriptions = new ArrayList<Subscription>()

        when: "The COM ECIM event is sent to be processed"
            comEcimEventChangeProcessor.processChangeEvent(comEcimNodeNotification, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()
    }

    def "Filtering is not executed when event operation type is unsupported"() {
        given: "A COM ECIM notification exists"
            ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification("NetworkElement=LTE04dg2ERBS00035", 2L, "2022-11-06T12:57:29Z", 170303357101647L, false)

        and: "The notification type is unsupported"
            comEcimNodeNotification.setAction(ComEcimNotificationType.MERGE)

        and: "Subscriptions exist in the database"
            List<Subscription> subscriptions = new ArrayList<Subscription>(){{
                add(new Subscription(new NtfSubscriptionControl(1, "https://site.com/eventListener/v10", Arrays.asList("notifyMOICreation", "notifyMOIDeletion", "notifyMOIAttributeValueChanges") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(3, "https://site.com/eventListener/v11", Arrays.asList("notifyMOICreation") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
                add(new Subscription(new NtfSubscriptionControl(4, "https://site.com/eventListener/v12", Arrays.asList("notifyMOIDeletion") as String[], "/", "/", "//MeContext", new Scope("BASE_ALL", 0))))
            }}

        when: "The COM ECIM event is sent to be processed"
            comEcimEventChangeProcessor.processChangeEvent(comEcimNodeNotification, subscriptions)

        then: "Filtering is not executed"
            0 * SubscriptionFilter.filter()

        and: "No exception is thrown"
            noExceptionThrown()

        and: "Debug log is logged"
            1 * logger.debug("Unsupported COM ECIM operation type: {}", comEcimNodeNotification)
    }
}