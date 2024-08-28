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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification
import com.ericsson.oss.mediation.network.api.notifications.NotificationType
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification
import com.ericsson.oss.mediation.notifications.ComEcimNotificationType
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.EventsInstrumentationBean
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.NtfSubscriptionControl
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Scope
import com.ericsson.oss.services.cmsubscribedevents.model.subscription.Subscription
import com.ericsson.oss.services.cmsubscribedevents.nbi.queue.EventQueue

import javax.inject.Inject

class EventNotificationProducerSpec extends CdiSpecification {

    public static final String EVENT_LISTENER_V1_URL = "/eventListener/v1"
    public static final String EVENT_LISTENER_V2_URL = "/eventListener/v2"
    public static final String EVENT_LISTENER_V3_URL = "/eventListener/v3"
    public static final String EVENT_LISTENER_V4_URL = "/eventListener/v4"
    public static final String HTTP_LOCALHOST = "http://localhost:"

    private static final String NOTIFY_MOI_CHANGES = "notifyMOIChanges"
    private static final String NOTIFY_MOI_CREATION = "notifyMOICreation"
    private static final String NOTIFY_MOI_DELETION = "notifyMOIDeletion"
    private static final String NOTIFY_MOI_AVC = "notifyMOIAttributeValueChanges"

    @ObjectUnderTest
    ComEcimEventNotificationProducer comEcimEventNotificationProducer
    @ObjectUnderTest
    CppEventNotificationProducer cppEventNotificationProducer
    @ObjectUnderTest
    DpsEventNotificationProducer dpsEventNotificationProducer

    @Inject
    EventsInstrumentationBean eventsInstrumentationBean

    def "A valid ComEcim notification matching a Subscription's notificationType is added to the EventQueue"() {

        given: "A valid ComEcimNodeNotification event"
            EventQueue.getInstance().getPushMap().clear()
            ComEcimNodeNotification comEcimNodeNotification = createComEcimNodeNotification()
            comEcimNodeNotification.setAction(action)

        and: "Subscription with notificationTypes are relevant to that of the incoming event"
            String[] notificationTypesArray = subscriptionNotificationType
            String notificationRecipientAddress = HTTP_LOCALHOST + eventListener

            List<Subscription> filteredSubscriptions = new ArrayList<Subscription>(){
                    {
                        add(new Subscription(new NtfSubscriptionControl(5, notificationRecipientAddress,  notificationTypesArray, "/", "/", "/", new Scope("BASE_ALL", 0))))
                    }
            }

        when: "Event sent off to be pushed to recipient address"
            comEcimEventNotificationProducer.publishEventToQueue(filteredSubscriptions, eventOperationType, comEcimNodeNotification)

        then: "Events to be pushed instrumentation is updated, and EventQueue has the correct data"
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiCreation() == createCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiDeletion() == deleteCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiAvc() == avcCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesCreate() == moiChangesCreateCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesDelete() == moiChangedDeleteCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesReplace() == moiChangesReplaceCount
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress).size() == 1

        where:
            subscriptionNotificationType | eventOperationType    | action                          | eventListener         | createCount | deleteCount | avcCount | moiChangesCreateCount | moiChangedDeleteCount | moiChangesReplaceCount
            [NOTIFY_MOI_CREATION]        | OperationType.CREATE  | ComEcimNotificationType.CREATE  | EVENT_LISTENER_V1_URL | 1           | 0           | 0        | 0                     | 0                     | 0
            [NOTIFY_MOI_DELETION]        | OperationType.DELETE  | ComEcimNotificationType.DELETE  | EVENT_LISTENER_V1_URL | 0           | 1           | 0        | 0                     | 0                     | 0
            [NOTIFY_MOI_AVC]             | OperationType.REPLACE | ComEcimNotificationType.UPDATE  | EVENT_LISTENER_V2_URL | 0           | 0           | 1        | 0                     | 0                     | 0
            [NOTIFY_MOI_CHANGES]         | OperationType.CREATE  | ComEcimNotificationType.CREATE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 1                     | 0                     | 0
            null                         | OperationType.CREATE  | ComEcimNotificationType.CREATE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 1                     | 0                     | 0
            [NOTIFY_MOI_CHANGES]         | OperationType.DELETE  | ComEcimNotificationType.DELETE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 0                     | 1                     | 0
            null                         | OperationType.DELETE  | ComEcimNotificationType.DELETE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 0                     | 1                     | 0
            [NOTIFY_MOI_CHANGES]         | OperationType.REPLACE | ComEcimNotificationType.UPDATE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 0                     | 0                     | 1
            null                         | OperationType.REPLACE | ComEcimNotificationType.UPDATE  | EVENT_LISTENER_V3_URL | 0           | 0           | 0        | 0                     | 0                     | 1
    }

    def "An incoming ComEcim notification processed against several Subscriptions will add notification to EventQueue for all respective recipient addresses"() {
        given: "an incoming ComEcim notification"
            EventQueue.getInstance().getPushMap().clear()
            ComEcimNodeNotification comEcimNodeNotification = createComEcimNodeNotification()
            comEcimNodeNotification.setAction(action)

        and: "Multiple Subscriptions on standby with varying notificationTypes"
            String notificationRecipientAddress1 = HTTP_LOCALHOST + eventListenerUrl
            String notificationRecipientAddress2 = HTTP_LOCALHOST + EVENT_LISTENER_V2_URL
            String notificationRecipientAddress3 = HTTP_LOCALHOST + EVENT_LISTENER_V3_URL
            String notificationRecipientAddress4 = HTTP_LOCALHOST + EVENT_LISTENER_V4_URL

            String [] notificationTypesArray = null
            if(subNotificationMatchingEventType != null) {
                notificationTypesArray = Arrays.asList(subNotificationMatchingEventType) as String[]
            }
            String [] allNotificationTypes = [NOTIFY_MOI_CREATION, NOTIFY_MOI_DELETION, NOTIFY_MOI_AVC, NOTIFY_MOI_CHANGES]

            List<Subscription> filteredSubscriptions = new ArrayList<Subscription>(){
                {
                    add(new Subscription(new NtfSubscriptionControl(3, notificationRecipientAddress1,  notificationTypesArray, "/", "/", "/", new Scope("BASE_ALL", 0))))
                    add(new Subscription(new NtfSubscriptionControl(5, notificationRecipientAddress2,  allNotificationTypes as String [], "/", "/", "/", new Scope("BASE_ALL", 0))))
                    add(new Subscription(new NtfSubscriptionControl(7, notificationRecipientAddress3,  Arrays.asList(NOTIFY_MOI_CHANGES) as String[], "/", "/", "/", new Scope("BASE_ALL", 0))))
                    add(new Subscription(new NtfSubscriptionControl(9, notificationRecipientAddress4,  null, "/", "/", "/", new Scope("BASE_ALL", 0))))
                }
            }

        when: "Event sent off to be pushed to recipient address"
            comEcimEventNotificationProducer.publishEventToQueue(filteredSubscriptions, eventOperationType, comEcimNodeNotification)

        then: "Events to be pushed instrumentation is updated, and EventQueue has the correct data"
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiCreation() == createCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiDeletion() == deleteCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiAvc() == avcCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesCreate() == moiChangesCreateCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesDelete() == moiChangedDeleteCount
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesReplace() == moiChangesReplaceCount
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress1).size() == 1
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress2).size() == 1
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress3).size() == 1
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress4).size() == 1

        where:
            eventListenerUrl      |  subNotificationMatchingEventType | eventOperationType    | action                         | createCount | deleteCount | avcCount | moiChangesCreateCount | moiChangedDeleteCount | moiChangesReplaceCount
            EVENT_LISTENER_V1_URL |  NOTIFY_MOI_CREATION              | OperationType.CREATE  | ComEcimNotificationType.CREATE | 1           | 0           | 0        | 3                     | 0                     | 0
            EVENT_LISTENER_V1_URL |  NOTIFY_MOI_DELETION              | OperationType.DELETE  | ComEcimNotificationType.DELETE | 0           | 1           | 0        | 0                     | 3                     | 0
            "/eventListener/v5"   |  NOTIFY_MOI_AVC                   | OperationType.REPLACE | ComEcimNotificationType.UPDATE | 0           | 0           | 1        | 0                     | 0                     | 3
    }

    def "dps and cpp notifications are pushed"() {
        given: "cpp and dps notification and empty data queue"
            EventQueue.getInstance().getPushMap().clear()
            NodeNotification nodeNotification = new NodeNotification()
            nodeNotification.setAction(NotificationType.CREATE)
            nodeNotification.setUpdateAttributes(new HashMap<String, String>(){
                {
                    put("userLabel", "Label")
                }
            })
            nodeNotification.setCreationTimestamp(new Date())
            nodeNotification.setFdn("MeContext=NR01gNodeBRadio00001,ManagedElement=1")
            DpsDataChangedEvent dpsDataChangedEvent = new DpsObjectCreatedEvent("OSS_NE_DEF", "NetworkElement", "2.0.0", 57000L, "NetworkElement=NR01gNodeBRadio00001",
                "Live", true, new HashMap<String, String>() {
                    {
                        put("attribute", "value")
                    }
            })

        and: "event listener"
            String notificationRecipientAddress = HTTP_LOCALHOST + EVENT_LISTENER_V2_URL
            List<Subscription> filteredSubscriptions = new ArrayList<Subscription>(){
                {
                    add(new Subscription(new NtfSubscriptionControl(1, notificationRecipientAddress,  Arrays.asList(NOTIFY_MOI_CHANGES) as String[], "/", "/", "/", new Scope("BASE_ALL", 0))))
                }
            }

        when: "Event sent off to be pushed to recipient address"
            cppEventNotificationProducer.publishEventToQueue(filteredSubscriptions, OperationType.CREATE, nodeNotification)
            dpsEventNotificationProducer.publishEventToQueue(filteredSubscriptions, OperationType.CREATE, dpsDataChangedEvent)

        then: "Events to be pushed instrumentation is updated, and EventQueue has the correct data"
            eventsInstrumentationBean.getVesEventsToBePushedNotifyMoiChangesCreate() == 2
            EventQueue.getInstance().getPushMap().get(notificationRecipientAddress).size() == 2
    }

    private static ComEcimNodeNotification createComEcimNodeNotification() {
        ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification("NetworkElement=LTE04dg2ERBS00035", 2L, "2023-04-06T01:29:42.472Z", 170303357101647L, asBoolean())
        comEcimNodeNotification.setUpdateAttributes(new HashMap<String, String>(){
            {
                put("userLabel", "Label")
            }
        })
        comEcimNodeNotification.setDn("SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00001,ManagedElement=NR01gNodeBRadio00001,GNBCUCPFunction=1,NRCellCU=3,NRCellRelation=1")

        return comEcimNodeNotification
    }
}
