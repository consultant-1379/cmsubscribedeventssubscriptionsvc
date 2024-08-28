/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2023
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */
package com.ericsson.oss.services.cmsubscribedevents.builder

import static com.ericsson.oss.services.cmsubscribedevents.constants.SubscriptionConstants.PRESENTATION_SERVER_NAME

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.notification.event.AttributeChangeData
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsAttributeChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsDataChangedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectCreatedEvent
import com.ericsson.oss.itpf.datalayer.dps.notification.event.DpsObjectDeletedEvent
import com.ericsson.oss.mediation.network.api.notifications.NodeNotification
import com.ericsson.oss.mediation.notifications.ComEcimNodeNotification
import com.ericsson.oss.services.cmsubscribedevents.enums.OperationType
import com.ericsson.oss.services.cmsubscribedevents.model.values.NotificationEventTypeValuesMoiHeartbeat
import com.ericsson.oss.services.cmsubscribedevents.model.ves.EventWrapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage

import java.text.SimpleDateFormat

import javax.inject.Inject

import org.slf4j.Logger

import spock.lang.Unroll

class VesEventBuilderSpec extends CdiSpecification {

    private static final String CONNECTION = "connection"
    private static final Date CURRENT_DATE = new Date()
    private static final String NOTIFY_MOICHANGES = "notifyMOIChanges"
    private static final String NOTIFY_MOICREATION = "notifyMOICreation"
    private static final String NOTIFY_MOIATTRIBUTE_VALUE_CHANGES = "notifyMOIAttributeValueChanges"
    private static final String NOTIFY_MOIDELETION = "notifyMOIDeletion"
    private static final SimpleDateFormat RECEIVED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private static final String EUTRANCELL_FDN = "SubNetwork=ENM1,MeContext=LTE01ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=1"
    private static final String REPORTING_ENTITY_NAME = "LTE01ERBS00001"
    private static final String SERVER_NAME = "serverName"
    private final static String NAMESPACE = "OSS_NE_DEF"
    private final static String NAME = "EUtranCellFDD"
    private final static String VERSION = "2.0.0"
    private final static Long PO_ID = 12345678910L
    private final static String BUCKET_NAME = "LIVE"
    private final static String DPS_FDN = "NetworkElement=LTE01ERBS00001"
    private static final String NETWORK_LINK_FDN = "Network=1,Link=" + NETWORK_LINK_REPORTING_ENTITY_NAME
    private static final String NETWORK_LINK_REPORTING_ENTITY_NAME = "Id-SPFRER60001/1/1-SPFRER60002/1/2"

    @ObjectUnderTest
    VesEventBuilder vesEventBuilder

    @Inject
    NotificationEventTypeValuesMoiHeartbeat notificationEventTypeValuesMOIHeartbeat

    def setupSpec() {
        System.setProperty(PRESENTATION_SERVER_NAME, SERVER_NAME)
    }

    def "When VesEventBuilder is called to build a heartbeat notification event a valid heartbeat notification event body is created it adheres to internal heartbeatSchema and ONAP schema"() {
        when: "Heartbeat content is returned"
            final EventWrapper eventWrapper = vesEventBuilder.createEvent(notificationEventTypeValuesMOIHeartbeat)

        then: "Heartbeat content is valid JSON format"
            validateHeartbeatJson(eventWrapper, "heartbeatSchema.json")
            validateHeartbeatJson(eventWrapper, "CommonEventFormat_30.2.1_ONAP.json")
    }

    def "When a COM ECIM notification is missing an expected value, then corresponding error is logged and return null"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = notification
            comEcimNodeNotification.setDn(dn)
            comEcimNodeNotification.setUpdateAttributes([string: "string"] as Map<String, Object>)
            vesEventBuilder.logger = Mock(Logger)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.CREATE, subscriptionNotificationType, comEcimNodeNotification)

        then: "Error is logged"
            vesEventBuilder.logger.error(errorMessageText)

        and: "EventWrapper is null"
            eventWrapper == null

        where:
            errorMessageText                                | notification                                                                                                                             | dn   | subscriptionNotificationType
            "Notification event doesn't contain a DN field" | new ComEcimNodeNotification(CONNECTION, 7L, RECEIVED_DATE_FORMAT.format(new Date()), new Random(Long.MAX_VALUE).nextLong(), asBoolean()) | null | NOTIFY_MOICHANGES
            "Notification event doesn't contain a DN field"                      | new ComEcimNodeNotification(CONNECTION, 7L, RECEIVED_DATE_FORMAT.format(new Date()), new Random(Long.MAX_VALUE).nextLong(), asBoolean())   | ""             | NOTIFY_MOICHANGES
            "Notification event doesn't contain a timestamp"                     | new ComEcimNodeNotification(CONNECTION, 7L, null, new Random(Long.MAX_VALUE).nextLong(), asBoolean())                                      | EUTRANCELL_FDN | NOTIFY_MOICHANGES
            "Notification event doesn't contain a timestamp"                     | new ComEcimNodeNotification(CONNECTION, 7L, "", new Random(Long.MAX_VALUE).nextLong(), asBoolean())                                        | EUTRANCELL_FDN | NOTIFY_MOICHANGES
            "Unsupported notification type for building the CommonNotifyMoiData" | new ComEcimNodeNotification(CONNECTION, 7L, RECEIVED_DATE_FORMAT.format(new Date()), new Random(Long.MAX_VALUE).nextLong(), asBoolean())   | EUTRANCELL_FDN | "invalid"

    }

    def "When a COM ECIM notification is missing notificationSequenceNumber, then corresponding debug log is logged and return EventWrapper with notificationSequenceNumber as 0"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification(CONNECTION, null, RECEIVED_DATE_FORMAT.format(new Date()), new Random(Long.MAX_VALUE).nextLong(), asBoolean())
            comEcimNodeNotification.setDn(EUTRANCELL_FDN)
            comEcimNodeNotification.setUpdateAttributes([string: "string"] as Map<String, Object>)
            vesEventBuilder.logger = Mock(Logger)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, comEcimNodeNotification)

        then: "Error is logged"
            vesEventBuilder.logger.debug("Notification sequence number is empty")

        and: "EventWrapper is returned with notification sequence number 0"
            eventWrapper.contains("\"sequence\":0")

    }

    def "When a CPP notification is missing an expected value, then corresponding error is logged"() {
        given: "A CPP notification is received"
            NodeNotification cppNodeNotification = setupCppNotification([:])
            cppNodeNotification.setFdn(fdn)
            cppNodeNotification.setCreationTimestamp(timestamp)
            vesEventBuilder.logger = Mock(Logger)

        when: "The CPP event is converted to a VES event"
            vesEventBuilder.buildCppVesEventString(OperationType.CREATE, subscriptionNotificationType, cppNodeNotification)

        then: "Error is logged"
            vesEventBuilder.logger.error(errorMessageText)

        where:
            errorMessageText                                 | timestamp    | fdn            | subscriptionNotificationType
            "Notification event doesn't contain a DN field"  | CURRENT_DATE | null           | NOTIFY_MOICHANGES
            "Notification event doesn't contain a DN field"  | CURRENT_DATE | ""             | NOTIFY_MOICHANGES
            "Notification event doesn't contain a timestamp" | null         | EUTRANCELL_FDN | NOTIFY_MOICHANGES
            "Unsupported notification type for building the CommonNotifyMoiData" | CURRENT_DATE | EUTRANCELL_FDN | "invalid"
    }

    def "When a DPS notification is missing an expected value, then corresponding error is logged"() {
        given: "A DPS notification with a missing value is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(operationType, [:])
            dpsDataChangedEvent.setFdn(fdn)
            vesEventBuilder.logger = Mock(Logger)

        when: "The DPS event is converted to a VES event"
            vesEventBuilder.buildDpsVesEventString(operationType, subscriptionNotificationType, dpsDataChangedEvent)

        then: "Error is logged"
            vesEventBuilder.logger.error(errorMessageText)

        where:
            errorMessageText                                | operationType         | fdn  | subscriptionNotificationType
            "Notification event doesn't contain a DN field" | OperationType.CREATE  | null | NOTIFY_MOICHANGES
            "Notification event doesn't contain a DN field" | OperationType.DELETE  | null | NOTIFY_MOICHANGES
            "Notification event doesn't contain a DN field" | OperationType.REPLACE | null | NOTIFY_MOICHANGES
            "Notification event doesn't contain a DN field"                      | OperationType.CREATE  | null    | NOTIFY_MOICREATION
            "Notification event doesn't contain a DN field"                      | OperationType.DELETE  | null    | NOTIFY_MOIDELETION
            "Notification event doesn't contain a DN field"                      | OperationType.REPLACE | null    | NOTIFY_MOIATTRIBUTE_VALUE_CHANGES
            "Unsupported notification type for building the CommonNotifyMoiData" | OperationType.REPLACE | DPS_FDN | "invalid"

    }

    def "When a COM ECIM notification is null, then error is logged"() {
        given: "A COM ECIM notification is null"
            ComEcimNodeNotification comEcimNodeNotification = null
            vesEventBuilder.logger = Mock(Logger)

        when: "The COM ECIM event converted to a VES event"
            vesEventBuilder.buildComEcimVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, comEcimNodeNotification)

        then: "Error is logged"
            1 * vesEventBuilder.logger.error("Notification event is null")
    }

    def "When a CPP notification is null, then error is logged"() {
        given: "A CPP notification is null"
            NodeNotification cppNotification = null
            vesEventBuilder.logger = Mock(Logger)

        when: "The CPP event converted to a VES event"
            vesEventBuilder.buildCppVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, cppNotification)

        then: "Error is logged"
            1 * vesEventBuilder.logger.error("Notification event is null")
    }

    def "When a DPS notification is null, then error is logged"() {
        given: "A DPS notification set to null is received"
            DpsDataChangedEvent dpsDataChangedEvent = null
            vesEventBuilder.logger = Mock(Logger)

        when: "The DPS event is converted to a VES event"
            vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICHANGES, dpsDataChangedEvent)

        then: "Error is logged"
            1 * vesEventBuilder.logger.error("Notification event is null")
    }

    @Unroll
    def "When a COM ECIM notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Notification Event validate it adheres to the internal vesNotificationEventsSchema and ONAP schema"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification(notificationAttributeMapValueContent)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, comEcimNodeNotification)

        then: "The converted COM ECIM notification is converted into a VES format that adheres to the internal vesNotificationEventsSchema and ONAP schema"
            validateJson(eventWrapper, "CommonEventFormat_30.2.1_ONAP.json")
            validateJson(eventWrapper, "vesNotificationEventSchema.json")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType      | notificationAttributeMapValueContent
            OperationType.CREATE              | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.CREATE              | NOTIFY_MOICREATION                | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOIATTRIBUTE_VALUE_CHANGES | ["string": "string"]
            OperationType.DELETE              | NOTIFY_MOICHANGES                 | [:]
            OperationType.DELETE              | NOTIFY_MOIDELETION                | [:]
    }

    @Unroll
    def "When a CPP notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Notification Event validate it adheres to the internal vesNotificationEventsSchema and ONAP schema"() {
        given: "A CPP notification is received"
            NodeNotification cppNotification = setupCppNotification(notificationAttributeMapValueContent)

        when: "The CPP notification converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, cppNotification)

        then: "The converted CPP notification is converted into a VES format that adheres to the internal vesNotificationEventsSchema and ONAP schema"
            validateJson(eventWrapper, "CommonEventFormat_30.2.1_ONAP.json")
            validateJson(eventWrapper, "vesNotificationEventSchema.json")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType      | notificationAttributeMapValueContent
            OperationType.CREATE              | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.CREATE              | NOTIFY_MOICREATION                | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOIATTRIBUTE_VALUE_CHANGES | ["string": "string"]
            OperationType.DELETE              | NOTIFY_MOICHANGES                 | [:]
            OperationType.DELETE              | NOTIFY_MOIDELETION                | [:]
    }

    @Unroll
    def "When a DPS notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Notification Event validate it adheres to the internal vesNotificationEventsSchema and ONAP schema"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(vesEventNotificationOperationType, notificationAttributeMapValueContent)

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, dpsDataChangedEvent)

        then: "The converted DPS notification is converted into a VES format that adheres to the internal vesNotificationEventsSchema and ONAP schema"
            validateJson(eventWrapper, "CommonEventFormat_30.2.1_ONAP.json")
            validateJson(eventWrapper, "vesNotificationEventSchema.json")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType      | notificationAttributeMapValueContent
            OperationType.CREATE              | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.CREATE              | NOTIFY_MOICREATION                | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOICHANGES                 | ["string": "string"]
            OperationType.REPLACE             | NOTIFY_MOIATTRIBUTE_VALUE_CHANGES | ["string": "string"]
            OperationType.DELETE              | NOTIFY_MOICHANGES                 | [:]
            OperationType.DELETE              | NOTIFY_MOIDELETION                | [:]
    }

    @Unroll
    def "When a COM ECIM notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification([:])

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, comEcimNodeNotification)

        then: "The converted COM ECIM notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted COM ECIM notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\""+NOTIFY_MOICHANGES+"\"")

        and: "The converted COM ECIM notification data contains expected MOI changes values"
            eventWrapper.contains("\"notificationType\":\""+NOTIFY_MOICHANGES+"\"")
            eventWrapper.contains("\"value\":[]")
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"path\":\"" + EUTRANCELL_FDN +"\"")

        where:
            vesEventNotificationOperationType | _
            OperationType.CREATE              | _
            OperationType.REPLACE             | _
            OperationType.DELETE              | _
    }

    @Unroll
    def "When a CPP notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A CPP notification is received"
            NodeNotification cppNotification = setupCppNotification([:])

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, cppNotification)

        then: "The converted CPP notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted CPP notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\""+NOTIFY_MOICHANGES+"\"")

        and: "The converted CPP notification data contains expected MOI changes values"
            eventWrapper.contains("\"value\":[]")
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"path\":\"" + EUTRANCELL_FDN +"\"")

        where:
            vesEventNotificationOperationType | _
            OperationType.CREATE              | _
            OperationType.REPLACE             | _
            OperationType.DELETE              | _
    }

    @Unroll
    def "When a DPS notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(vesEventNotificationOperationType, [:])

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, dpsDataChangedEvent)

        then: "The converted DPS notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted DPS notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\""+NOTIFY_MOICHANGES+"\"")

        and: "The converted DPS notification data contains expected MOI changes values"
            eventWrapper.contains("\"value\":[]")
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"path\":\"" + DPS_FDN +"\"")

        where:
            vesEventNotificationOperationType | _
            OperationType.CREATE              | _
            OperationType.REPLACE             | _
            OperationType.DELETE              | _
    }

    @Unroll
    def "When a COM ECIM notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification([:])

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, comEcimNodeNotification)

        then: "The converted COM ECIM notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted COM ECIM notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")

        and: "The converted COM ECIM notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeList\":[]")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")
            eventWrapper.contains("\"href\":\"" + EUTRANCELL_FDN +"\"")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType
            OperationType.CREATE              | NOTIFY_MOICREATION
            OperationType.DELETE              | NOTIFY_MOIDELETION
    }

    @Unroll
    def "When a CPP notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A CPP notification is received"
            NodeNotification cppNotification = setupCppNotification([:])

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, cppNotification)

        then: "The converted CPP notification common header contains the expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted CPP notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")

        and: "The converted CPP notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeList\":[]")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")
            eventWrapper.contains("\"href\":\"" + EUTRANCELL_FDN +"\"")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType
            OperationType.CREATE              | NOTIFY_MOICREATION
            OperationType.DELETE              | NOTIFY_MOIDELETION
    }

    @Unroll
    def "When a DPS notification for a #vesEventNotificationOperationType event and a subscription with #subscriptionNotificationType is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(vesEventNotificationOperationType, [:])

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(vesEventNotificationOperationType, subscriptionNotificationType, dpsDataChangedEvent)

        then: "The converted DPS notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted DPS notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")

        and: "The converted DPS notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeList\":[]")
            eventWrapper.contains("\"notificationType\":\"" + subscriptionNotificationType +"\"")
            eventWrapper.contains("\"href\":\"" + DPS_FDN +"\"")

        where:
            vesEventNotificationOperationType | subscriptionNotificationType
            OperationType.CREATE              | NOTIFY_MOICREATION
            OperationType.DELETE              | NOTIFY_MOIDELETION
    }

    def "When a COM ECIM notification for a REPLACE event and a subscription with 'notifyMOIAttributeValueChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification([:])

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, comEcimNodeNotification)

        then: "The converted COM ECIM notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted COM ECIM notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + NOTIFY_MOIATTRIBUTE_VALUE_CHANGES +"\"")

        and: "The converted COM ECIM notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeListValueChanges\":[]")
    }

    def "When a CPP notification for a REPLACE event and a subscription with 'notifyMOIAttributeValueChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A CPP notification is received"
            NodeNotification cppNotification = setupCppNotification([:])

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, cppNotification)

        then: "The converted CPP notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted CPP notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + NOTIFY_MOIATTRIBUTE_VALUE_CHANGES +"\"")

        and: "The converted CPP notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeListValueChanges\":[]")
    }

    def "When a DPS notification for a REPLACE event and a subscription with 'notifyMOIAttributeValueChanges' is converted to VES Event with empty attribute map it contains expected content"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(OperationType.REPLACE, [:])

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, dpsDataChangedEvent)

        then: "The converted DPS notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+REPORTING_ENTITY_NAME+"\"")

        and: "The converted DPS notification common header contains expected data values"
            eventWrapper.contains("\"systemDN\":\"" + SERVER_NAME +"\"")
            eventWrapper.contains("\"notificationType\":\"" + NOTIFY_MOIATTRIBUTE_VALUE_CHANGES +"\"")

        and: "The converted DPS notification data contains expected provisioning changes values"
            eventWrapper.contains("\"attributeListValueChanges\":[]")
    }

    @Unroll
    def "When a COM ECIM notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification(updatedAttributeMap)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, comEcimNodeNotification)

        then: "The converted COM ECIM notification data content contains expected values"
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"value\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            vesEventNotificationOperationType | notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            OperationType.CREATE              | "string"                           | "stringValue"
            OperationType.REPLACE             | "string"                           | "stringValue"
            OperationType.CREATE              | "enum"                             | OperationType.CREATE
            OperationType.REPLACE             | "enum"                             | OperationType.CREATE
            OperationType.CREATE              | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.REPLACE             | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.CREATE              | "int"                              | 123
            OperationType.REPLACE             | "int"                              | 123
            OperationType.CREATE              | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.REPLACE             | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.CREATE              | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.REPLACE             | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.CREATE              | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.REPLACE             | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.CREATE              | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
            OperationType.REPLACE             | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a CPP notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A CPP notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            NodeNotification cppNotification = setupCppNotification(updatedAttributeMap)

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, cppNotification)

        then: "The converted CPP notification data content contains expected values"
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"value\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            vesEventNotificationOperationType | notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            OperationType.CREATE              | "string"                           | "stringValue"
            OperationType.REPLACE             | "string"                           | "stringValue"
            OperationType.CREATE              | "enum"                             | OperationType.CREATE
            OperationType.REPLACE             | "enum"                             | OperationType.CREATE
            OperationType.CREATE              | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.REPLACE             | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.CREATE              | "int"                              | 123
            OperationType.REPLACE             | "int"                              | 123
            OperationType.CREATE              | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.REPLACE             | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.CREATE              | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.REPLACE             | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.CREATE              | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.REPLACE             | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.CREATE              | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
            OperationType.REPLACE             | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a DPS notification for a #vesEventNotificationOperationType event and a subscription with 'notifyMOIChanges' is converted to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A DPS notification is received"
            Map<String, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(vesEventNotificationOperationType, updatedAttributeMap)

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, dpsDataChangedEvent)

        then: "The converted DPS notification data content contains expected values"
            eventWrapper.contains("\"operation\":\"" + vesEventNotificationOperationType.toString() +"\"")
            eventWrapper.contains("\"value\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            vesEventNotificationOperationType | notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            OperationType.CREATE              | "string"                           | "stringValue"
            OperationType.REPLACE             | "string"                           | "stringValue"
            OperationType.CREATE              | "enum"                             | OperationType.CREATE
            OperationType.REPLACE             | "enum"                             | OperationType.CREATE
            OperationType.CREATE              | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.REPLACE             | "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            OperationType.CREATE              | "int"                              | 123
            OperationType.REPLACE             | "int"                              | 123
            OperationType.CREATE              | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.REPLACE             | "list"                             | Arrays.asList("listEntry1", "listEntry2")
            OperationType.CREATE              | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.REPLACE             | "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            OperationType.CREATE              | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.REPLACE             | "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            OperationType.CREATE              | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
            OperationType.REPLACE             | "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a COM ECIM notification for a subscription with 'notifyMOICreation' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification(updatedAttributeMap)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, comEcimNodeNotification)

        then: "The converted COM ECIM notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.CREATE.notificationType +"\"")
            eventWrapper.contains("\"attributeList\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a CPP notification for a subscription with 'notifyMOICreation' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A CPP notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            NodeNotification cppNotification = setupCppNotification(updatedAttributeMap)

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, cppNotification)

        then: "The converted CPP notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.CREATE.notificationType +"\"")
            eventWrapper.contains("\"attributeList\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a DPS notification for a subscription with 'notifyMOICreation' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A DPS notification is received"
            Map<String, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(OperationType.CREATE, updatedAttributeMap)

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, dpsDataChangedEvent)

        then: "The converted DPS notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.CREATE.notificationType +"\"")
            eventWrapper.contains("\"attributeList\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a COM ECIM notification for a subscription with 'notifyMOIAttributeValueChanges' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A COM ECIM notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            ComEcimNodeNotification comEcimNodeNotification = setupComEcimNotification(updatedAttributeMap)

        when: "The COM ECIM event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, comEcimNodeNotification)

        then: "The converted COM ECIM notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.REPLACE.notificationType +"\"")
            eventWrapper.contains("\"attributeListValueChanges\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a CPP notification for a subscription with 'notifyMOIAttributeValueChanges' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A CPP notification is received"
            Map<Object, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            NodeNotification cppNotification = setupCppNotification(updatedAttributeMap)

        when: "The CPP event converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, cppNotification)

        then: "The converted CPP notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.REPLACE.notificationType +"\"")
            eventWrapper.contains("\"attributeListValueChanges\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    @Unroll
    def "When a DPS notification for a subscription with 'notifyMOIAttributeValueChanges' to VES Event with #notificationAttributeMapKeyContent attribute map it contains expected content"() {
        given: "A DPS notification is received"
            Map<String, Object> updatedAttributeMap = new HashMap<>()
            updatedAttributeMap.put(notificationAttributeMapKeyContent, notificationAttributeMapValueContent)
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(OperationType.REPLACE, updatedAttributeMap)

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.REPLACE, NOTIFY_MOIATTRIBUTE_VALUE_CHANGES, dpsDataChangedEvent)

        then: "The converted DPS notification data content contains expected values"
            eventWrapper.contains("\"notificationType\":\"" + OperationType.REPLACE.notificationType +"\"")
            eventWrapper.contains("\"attributeListValueChanges\":"+ convertUpdateAttributesToJson(updatedAttributeMap))

        where:
            notificationAttributeMapKeyContent | notificationAttributeMapValueContent
            "string"                           | "stringValue"
            "enum"                             | OperationType.CREATE
            "enumList"                         | Arrays.asList(OperationType.CREATE, OperationType.DELETE, null)
            "int"                              | 123
            "list"                             | Arrays.asList("listEntry1", "listEntry2")
            "listOfLists"                      | Arrays.asList(Arrays.asList("stringList1Entry1", "stringList1Entry2") as Object, Arrays.asList("stringList2Entry1", "stringList2Entry2"))
            "array"                            | ["stringArrayEntry1", "stringArrayEntry2"]
            "stringMap"                        | ["stringMap": ["stringMapKey1": "stringMapValue1", "stringMapKey2": "stringMapValue2", "stringMapKey3": ""]]
    }

    def "When a COM ECIM notification with a valid timestamp is converted to a VES Event it contains the expected UTC timestamp"() {
        given: "A COM ECIM notification"
            final ComEcimNodeNotification comEcimNodeNotification = new ComEcimNodeNotification(CONNECTION, new Random(Long.MAX_VALUE).nextLong(), inputTimestamp, new Random(Long.MAX_VALUE).nextLong(), asBoolean())
            comEcimNodeNotification.setDn(EUTRANCELL_FDN)
            comEcimNodeNotification.setUpdateAttributes(["string": "string"] as Map<String, Object>)
            TimeZone.setDefault(timeZone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZone))

        when: "The COM ECIM event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildComEcimVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, comEcimNodeNotification)

        then: "The timestamp contains the expected UTC time and date"
            eventWrapper.contains("\"eventTime\":\"" + expectedTimestamp +"\"")

        where:
            inputTimestamp             | expectedTimestamp          | timeZone
            "2016-04-24T11:00:00Z"     | "2016-04-24T11:00:00.000Z" | null
            "2016-04-24T11:00:00"      | "2016-04-24T11:00:00.000Z" | null
            "2016-04-24T11:00:00-0100" | "2016-04-24T12:00:00.000Z" | null
            "2016-04-24T13:00:00-1200" | "2016-04-25T01:00:00.000Z" | null
            "2016-04-24T11:00:00+0100" | "2016-04-24T10:00:00.000Z" | null
            "2016-04-24T11:00:00+1200" | "2016-04-23T23:00:00.000Z" | null
            "2016-04-24T11:00:00-0100" | "2016-04-24T12:00:00.000Z" | "Asia/Tokyo"
            "2016-04-24T13:00:00-1200" | "2016-04-25T01:00:00.000Z" | "Asia/Tokyo"
            "2016-04-24T11:00:00-0100" | "2016-04-24T12:00:00.000Z" | "America/Los_Angeles"
            "2016-04-24T13:00:00-1200" | "2016-04-25T01:00:00.000Z" | "America/Los_Angeles"
    }

    def "When a CPP notification with a valid timestamp is converted to a VES Event it contains the expected UTC timestamp"() {
        given: "A CPP notification"
            NodeNotification cppNotification = setupCppNotification([:])
            TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
            cppNotification.setCreationTimestamp(simpleDateFormat.parse(inputTimestamp))

        when: "The CPP event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildCppVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, cppNotification)

        then: "The timestamp contains the expected UTC time and date"
            eventWrapper.contains("\"eventTime\":\"" + expectedTimestamp +"\"")

        where:
            inputTimestamp                 | expectedTimestamp          | timeZone
            "Sun Nov 06 13:00:00 UTC 2022" | "2022-11-06T13:00:00.000Z" | "UTC"
            "Sun Nov 06 13:00:00 IST 2022" | "2022-11-06T13:00:00.000Z" | "IST"
            "Sun Nov 06 13:00:00 JST 2022" | "2022-11-06T13:00:00.000Z" | "Asia/Tokyo"
            "Sun Nov 06 05:00:00 PST 2022" | "2022-11-06T05:00:00.000Z" | "America/Los_Angeles"
    }

    def "When a DPS notification is received a valid timestamp is generated"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(OperationType.CREATE, [:])

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(OperationType.CREATE, NOTIFY_MOICREATION, dpsDataChangedEvent)

        then: "The timestamp contains the expected UTC time and date"
            final ObjectMapper objectMapper = new ObjectMapper()
            String eventTime = objectMapper.readTree(eventWrapper).findValue("event").findValue("stndDefinedFields").findValue("data").get("eventTime").textValue()
            eventTime ==~ /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/
    }

    def "When a DPS notification for a Network Link MO event is received it contains the expected content"() {
        given: "A DPS notification is received"
            DpsDataChangedEvent dpsDataChangedEvent = setupDpsNotification(vesEventNotificationOperationType, [:])
            dpsDataChangedEvent.setFdn(NETWORK_LINK_FDN)

        when: "The DPS event is converted to a VES event"
            String eventWrapper = vesEventBuilder.buildDpsVesEventString(vesEventNotificationOperationType, NOTIFY_MOICHANGES, dpsDataChangedEvent)

        then: "The converted DPS notification common header contains expected values"
            eventWrapper.contains("\"reportingEntityName\":\""+NETWORK_LINK_REPORTING_ENTITY_NAME+"\"")

        and: "The converted DPS notification data contains expected MOI changes values"
            eventWrapper.contains("\"href\":\""+NETWORK_LINK_FDN+"\"")

        where:
            vesEventNotificationOperationType | _
            OperationType.CREATE              | _
            OperationType.REPLACE             | _
            OperationType.DELETE              | _
    }

    private static boolean validateHeartbeatJson(EventWrapper eventWrapper, String schema) {
        final ObjectMapper objectMapper = new ObjectMapper()
        String notificationBody = objectMapper.writeValueAsString(eventWrapper)
        final JsonNode jsonNode = objectMapper.readTree(notificationBody)
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        InputStream schemaStream = classLoader.getResourceAsStream(schema)
        final JsonSchema notificationSchema = schemaFactory.getSchema(schemaStream)
        Set<ValidationMessage> validationResult = notificationSchema.validate(jsonNode)
        validationResult.isEmpty()
    }

    private static boolean validateJson(String notificationBody, String schema) {
        final ObjectMapper objectMapper = new ObjectMapper()
        final JsonNode jsonNode = objectMapper.readTree(notificationBody)
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        InputStream schemaStream = classLoader.getResourceAsStream(schema)
        final JsonSchema notificationSchema = schemaFactory.getSchema(schemaStream)
        Set<ValidationMessage> validationResult = notificationSchema.validate(jsonNode)
        validationResult.isEmpty()
    }

    private static ComEcimNodeNotification setupComEcimNotification(final HashMap<Object, Object> attributesForOperation) {
        final ComEcimNodeNotification notificationEvent = new ComEcimNodeNotification(CONNECTION, new Random(Long.MAX_VALUE).nextLong(), RECEIVED_DATE_FORMAT.format(new Date()), new Random(Long.MAX_VALUE).nextLong(), asBoolean())
        notificationEvent.setDn(EUTRANCELL_FDN)
        notificationEvent.setUpdateAttributes(attributesForOperation as Map<String, Object>)
        return notificationEvent
    }

    private static NodeNotification setupCppNotification(final HashMap<Object, Object> attributesForOperation) {
        final NodeNotification nodeNotification = new NodeNotification()
        nodeNotification.setFdn(EUTRANCELL_FDN)
        nodeNotification.setUpdateAttributes(attributesForOperation as Map<String, Object>)
        nodeNotification.setCreationTimestamp(CURRENT_DATE)
        return nodeNotification
    }

    private static DpsDataChangedEvent setupDpsNotification(final OperationType operationType, final Map<String, Object> attributesForOperation) {
        switch (operationType) {
            case OperationType.REPLACE:
                return new DpsAttributeChangedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, convertAttributesForOperationToAttributeChangeData(attributesForOperation))
            case OperationType.DELETE:
                return new DpsObjectDeletedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, false, attributesForOperation)
            case OperationType.CREATE:
                return new DpsObjectCreatedEvent(NAMESPACE, NAME, VERSION, PO_ID, DPS_FDN, BUCKET_NAME, false, attributesForOperation)
            default:
                return null
        }
    }

    private static Collection<AttributeChangeData> convertAttributesForOperationToAttributeChangeData(final Map<String, Object> attributesMapToBeConverted) {
        final Collection<AttributeChangeData> attributeChangeDataCollection = new ArrayList<>()
        for (final String attributeName : attributesMapToBeConverted.keySet()) {
            final AttributeChangeData attributeChangeData = new AttributeChangeData()
            attributeChangeData.setName(attributeName)
            attributeChangeData.setNewValue(attributesMapToBeConverted.get(attributeName))
            attributeChangeDataCollection.add(attributeChangeData)
        }
        return attributeChangeDataCollection
    }

    private static convertUpdateAttributesToJson(Map<String, Object> updatedAttributeMap) {
        final List<Map<String, Object>> listOfUpdateAttributes = new ArrayList<>()
        listOfUpdateAttributes.add(updatedAttributeMap);
        return new ObjectMapper().writeValueAsString(listOfUpdateAttributes);
    }
}

